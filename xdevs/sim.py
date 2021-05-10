from __future__ import annotations

import _thread
import itertools
import json
import pickle
import socket

from abc import ABC, abstractmethod
from collections import defaultdict
from concurrent import futures
from io import StringIO
from typing import Dict, Generator, List, NoReturn, Optional, Union
from xmlrpc.server import SimpleXMLRPCServer

from lxml import etree

from xdevs import INFINITY
from xdevs.models import Atomic, Coupled, Component, Port, T
from xdevs.transducers import Transducer


class SimulationClock:
    def __init__(self, time: float = 0):
        self.time: float = time


class AbstractSimulator(ABC):
    def __init__(self, model: Component, clock: SimulationClock,
                 event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None):
        self.model: Component = model
        self.clock: SimulationClock = clock
        self.time_last: float = 0
        self.time_next: float = 0

        self.event_transducers: Optional[Dict[Port, List[Transducer]]] = None
        if event_transducers_mapping:
            port_transducers: Dict[Port, List[Transducer]] = dict()
            for port in itertools.chain(self.model.in_ports, self.model.out_ports):
                transducers = event_transducers_mapping.get(port, None)
                if transducers:
                    port_transducers[port] = transducers
            if port_transducers:
                self.event_transducers = port_transducers

    @property
    def imminent(self) -> bool:
        return self.clock.time == self.time_next or not self.model.in_empty()

    def trigger_event_transducers(self):
        if self.event_transducers is not None:
            for port, transducers in self.event_transducers.items():
                if port:  # Only for ports with messages
                    for trans in transducers:
                        trans.add_imminent_port(port)

    @abstractmethod
    def initialize(self) -> NoReturn:
        pass

    @abstractmethod
    def exit(self) -> NoReturn:
        pass

    @abstractmethod
    def ta(self) -> float:
        pass

    @abstractmethod
    def lambdaf(self) -> NoReturn:
        pass

    @abstractmethod
    def deltfcn(self) -> Optional[AbstractSimulator]:
        pass

    @abstractmethod
    def clear(self) -> NoReturn:
        pass


class Simulator(AbstractSimulator):

    model: Atomic

    def __init__(self, model: Atomic, clock: SimulationClock,
                 event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None,
                 state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]] = None):
        super().__init__(model, clock, event_transducers_mapping)
        self.state_transducers: Optional[List[Transducer]] = None
        if state_transducers_mapping:
            self.state_transducers = state_transducers_mapping.get(self.model, None)

    @property
    def ta(self) -> float:
        return self.model.ta

    def initialize(self) -> NoReturn:
        self.model.initialize()
        self.time_last = self.clock.time
        self.time_next = self.time_last + self.model.ta

    def exit(self) -> NoReturn:
        self.model.exit()

    def deltfcn(self) -> Optional[Simulator]:  # TODO
        if not self.model.in_empty():
            e = self.clock.time - self.time_last
            if self.clock.time == self.time_next:
                self.model.deltcon(e)
            else:
                self.model.deltext(e)
        elif self.clock.time == self.time_next:
            self.model.deltint()
        else:
            return

        if self.state_transducers is not None:
            for trans in self.state_transducers:
                trans.add_imminent_model(self.model)

        self.trigger_event_transducers()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.model.ta
        return self

    def lambdaf(self) -> NoReturn:
        if self.clock.time == self.time_next:
            self.model.lambdaf()

    def clear(self) -> NoReturn:
        for port in itertools.chain(self.model.in_ports, self.model.out_ports):
            port.clear()


class Coordinator(AbstractSimulator):

    model: Coupled

    def __init__(self, model: Coupled, clock: SimulationClock = None, flatten: bool = False,
                 event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None,
                 state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]] = None):
        super().__init__(model, clock or SimulationClock(), event_transducers_mapping)

        self.coordinators: List[Coordinator] = list()
        self.simulators: List[Simulator] = list()
        self._transducers: Optional[List[Transducer]] = [] if self.root_coordinator else None

        if flatten:
            self.model.flatten()
        self.ports_to_serve = dict()

        self.__event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None
        self.__state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]] = None
        if not self.root_coordinator:
            # Only non-root coordinators will load the transducers mapping in the constructor.
            # Root coordinator ignores them, as it is in charge of building them in _build_hierarchy
            self.event_transducers_mapping = event_transducers_mapping
            self.state_transducers_mapping = state_transducers_mapping

    @property
    def root_coordinator(self) -> bool:
        return self.model.parent is None

    @property
    def event_transducers_mapping(self) -> Optional[Dict[Port, List[Transducer]]]:
        return self.__event_transducers_mapping

    @property
    def state_transducers_mapping(self) -> Optional[Dict[Atomic, List[Transducer]]]:
        return self.__state_transducers_mapping

    @event_transducers_mapping.setter
    def event_transducers_mapping(self, event_transducers_mapping: Optional[Dict[Port, List[Transducer]]]):
        if event_transducers_mapping:
            self.__event_transducers_mapping = event_transducers_mapping

    @state_transducers_mapping.setter
    def state_transducers_mapping(self, state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]]):
        if state_transducers_mapping:
            self.__state_transducers_mapping = state_transducers_mapping

    @property
    def processors(self) -> Generator[AbstractSimulator, None, None]:
        for coord in self.coordinators:
            yield coord
        for sim in self.simulators:
            yield sim

    @property
    def imminent_processors(self) -> Generator[AbstractSimulator, None, None]:
        return (proc for proc in self.processors if proc.imminent)

    def initialize(self):
        self._build_hierarchy()

        for proc in self.processors:
            proc.initialize()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()

        if self.root_coordinator:
            for transducer in self._transducers:
                transducer.initialize()

    def _build_hierarchy(self):
        if self.root_coordinator and self._transducers:
            # The root coordinator is in charge of
            ports_to_transducers: Dict[Port, List[Transducer]] = defaultdict(list)
            models_to_transducers: Dict[Atomic, List[Transducer]] = defaultdict(list)
            for transducer in self._transducers:
                for model in transducer.target_components:
                    models_to_transducers[model].append(transducer)
                for port in transducer.target_ports:
                    ports_to_transducers[port].append(transducer)
            self.event_transducers_mapping = ports_to_transducers
            self.state_transducers_mapping = models_to_transducers

        for comp in self.model.components:
            if isinstance(comp, Coupled):
                coord = Coordinator(comp, self.clock, event_transducers_mapping=self.event_transducers_mapping,
                                    state_transducers_mapping=self.state_transducers_mapping)
                self.coordinators.append(coord)
                self.ports_to_serve.update(coord.ports_to_serve)
            elif isinstance(comp, Atomic):
                sim = Simulator(comp, self.clock, event_transducers_mapping=self.event_transducers_mapping,
                                state_transducers_mapping=self.state_transducers_mapping)
                self.simulators.append(sim)
                for pts in sim.model.in_ports:
                    if pts.serve:
                        port_name = "%s.%s" % (pts.parent.name, pts.name)
                        self.ports_to_serve[port_name] = pts

    def add_transducer(self, transducer: Transducer):
        if not self.root_coordinator:
            raise RuntimeError('Only the root coordinator can contain transducers')
        self._transducers.append(transducer)

    def serve(self, host: str = "localhost", port: int = 8000):
        server = SimpleXMLRPCServer((host, port))
        server.register_function(self.inject)
        _thread.start_new_thread(server.serve_forever, ())

    def exit(self):
        for processor in self.processors:
            processor.exit()

        for transducer in self._transducers:
            transducer.exit()

    def ta(self):
        return min((proc.time_next for proc in self.processors), default=INFINITY) - self.clock.time

    def lambdaf(self) -> NoReturn:
        for proc in self.processors:
            if self.clock.time == proc.time_next:
                proc.lambdaf()
                self.propagate_output(proc.model)

    def propagate_output(self, comp: Component):
        for port in comp.used_out_ports:
            for coup in itertools.chain(self.model.ic.get(port, dict()).values(),
                                        self.model.eoc.get(port, dict()).values()):
                coup.propagate()

    def deltfcn(self):
        self.propagate_input()

        for proc in self.imminent_processors:
            proc.deltfcn()

        self.trigger_event_transducers()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()

    def propagate_input(self):
        for port in self.model.used_in_ports:
            for coup in self.model.eic.get(port, dict()).values():
                coup.propagate()

    def clear(self) -> NoReturn:
        for port in itertools.chain(self.processors, self.model.in_ports, self.model.out_ports):
            port.clear()

    def inject(self, port: Union[str, Port[T]], values: Union[T, List[T]], e: float = 0) -> bool:
        # TODO enable any iterable as values (careful with str)
        time = self.time_last + e

        if type(values) is not list:
            values = [values]

        if isinstance(port, str):
            values = list(map(lambda x: pickle.loads(x.encode()), values))
            if port in self.ports_to_serve:
                port = self.ports_to_serve[port]
            else:
                # logger.error("Port '%s' not found" % port)
                return True  # TODO is this OK?

        if time <= self.time_next or time != time:
            port.extend(values)
            self.clock.time = time
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next
            return True
        else:
            # logger.error("Time %d - Input rejected: elapsed time %d is not in bounds" % (self.time_last, e))
            return False

    def simulate(self, num_iters: int = 10000):
        self.clock.time = self.time_next
        cont = 0

        while cont < num_iters and self.clock.time < INFINITY:
            self.lambdaf()
            self.deltfcn()
            self._execute_transducers()
            self.clear()
            self.clock.time = self.time_next
            cont += 1

    def simulate_time(self, time_interv: float = 10000):
        self.clock.time = self.time_next
        tf = self.clock.time + time_interv

        while self.clock.time < tf:
            self.lambdaf()
            self.deltfcn()
            self._execute_transducers()
            self.clear()
            self.clock.time = self.time_next

    def simulate_inf(self):
        while True:
            self.lambdaf()
            self.deltfcn()
            self._execute_transducers()
            self.clear()
            self.clock.time = self.time_next

    def _execute_transducers(self):
        for transducer in self._transducers:
            transducer.trigger(self.clock.time)


# TODO we should review the parallel implementation


class ParallelCoordinator(Coordinator):
    def __init__(self, model: Coupled, clock: SimulationClock = None, flatten: bool = False,
                 event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None,
                 state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]] = None, executor=None):
        super().__init__(model, clock, flatten, event_transducers_mapping=event_transducers_mapping,
                         state_transducers_mapping=state_transducers_mapping)

        self.executor = executor or futures.ThreadPoolExecutor(max_workers=8)  # TODO calc max workers

    def _add_coordinator(self, coupled: Coupled):
        coord = ParallelCoordinator(coupled, self.clock, executor=False)
        self.coordinators.append(coord)
        self.ports_to_serve.update(coord.ports_to_serve)

    def _lambdaf(self):
        for coord in self.coordinators:
            coord.lambdaf()
        ex_futures = []
        for sim in self.simulators:
            self.add_task_to_pool(sim.lambdaf)

        for future in futures.as_completed(ex_futures):
            future.result()

        self.propagate_output()

    def deltfcn(self):
        self.propagate_input()

        for coord in self.coordinators:
            coord.deltfcn()
        ex_futures = []
        for sim in self.simulators:
            self.add_task_to_pool(sim.deltfcn)

        for future in futures.as_completed(ex_futures):
            future.result()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()

    def add_task_to_pool(self, task):
        self.executor.submit(task)


class ParallelProcessCoordinator(Coordinator):

    coordinators: List[ParallelProcessCoordinator]

    def __init__(self, model: Coupled, clock: SimulationClock = None,
                 event_transducers_mapping: Optional[Dict[Port, List[Transducer]]] = None,
                 state_transducers_mapping: Optional[Dict[Atomic, List[Transducer]]] = None,
                 master=True, executor=None, executor_futures=None):
        super().__init__(model, clock, event_transducers_mapping=event_transducers_mapping,
                         state_transducers_mapping=state_transducers_mapping)
        self.master = master

        if master:
            self.executor = futures.ProcessPoolExecutor()
            self.executor_futures = dict()
        else:
            self.executor = executor
            self.executor_futures = executor_futures

    def _add_coordinator(self, coupled: Coupled):
        coord = ParallelProcessCoordinator(coupled, self.clock, master=False, executor=self.executor,
                                           executor_futures=self.executor_futures)
        self.coordinators.append(coord)
        self.ports_to_serve.update(coord.ports_to_serve)

    def lambdaf(self):

        for coord in self.coordinators:
            if coord.clock.time == coord.time_next:
                coord.lambdaf()

        for sim in self.simulators:
            if sim.clock.time == sim.time_next:
                self.executor_futures[self.executor.submit(sim.lambdaf)] = (self, sim)

        if self.master:
            for i, future in enumerate(self.executor_futures):
                # logger.debug("D: Waiting... (%d/%d)" % (i+1, len(self.executor_futures)))
                futures.wait((future,))

                res = future.result()
                if isinstance(res, Simulator):
                    coord, sim = self.executor_futures[future]
                    for model_port, new_model_port in zip(sim.model.out_ports, future.result().model.out_ports):
                        model_port.extend(list(new_model_port.values))

            self.executor_futures.clear()
            self.propagate_output()

    def deltfcn(self):
        if self.master:
            self.propagate_input()

        for coord in self.coordinators:
            coord.deltfcn()

        for sim in self.simulators:
            self.executor_futures[self.executor.submit(sim.deltfcn)] = (self, sim)

        if self.master:
            for i, future in enumerate(self.executor_futures):
                # logger.debug("D: Waiting... (%d/%d)" % (i+1, len(self.executor_futures)))
                futures.wait((future,))

                res = future.result()
                if isinstance(res, Simulator):
                    coord, sim = self.executor_futures[future]
                    model = sim.model
                    new_sim = future.result()
                    new_model = new_sim.model

                    # Copy new state
                    if hasattr(new_model, "__state__"):
                        for state_att in new_model.__state__:
                            setattr(model, state_att, getattr(new_model, state_att))

                    # Update simulator info
                    sim.model = model
                    sim.time_last = new_sim.time_last
                    sim.time_next = new_sim.time_next

            self.executor_futures.clear()
            self.update_times()

    def propagate_output(self):
        for coord in self.coordinators:
            coord.propagate_output()

        super().propagate_output()

    def propagate_input(self):
        super().propagate_input()

        for coord in self.coordinators:
            coord.propagate_input()

    def update_times(self):
        for coord in self.coordinators:
            coord.update_times()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()
        # logger.debug({proc.model.name:proc.time_next for proc in self.processors})
        # logger.debug("Deltfcn %s: TL: %s, TN: %s" % (self.model.name, self.time_last, self.time_next))


class DistributedSimulator(Simulator):
    CMD_INITIALIZE = "initialize"
    CMD_TA = "ta"
    CMD_LAMBDA = "lambda"
    CMD_INJECT = "inject"
    CMD_PROPAGATE_OUT = "propagate_output"
    CMD_DELTFCN = "deltfcn"
    CMD_CLEAR = "clear"
    CMD_EXIT = "exit"

    ERR_INVALID_MSG = "invalid_msg"
    ERR_INVALID_PORT = "invalid_port"
    ERR_NO_CMD = "no_cmd"
    ERR_BAD_CMD = "bad_cmd"

    STATUS_OK = "ok"
    STATUS_ERR = "error"
    STATUS_EXIT = "exit"

    def __init__(self, model_xml: str, model_name: str, buff_size=1024):
        self.buff_size = buff_size
        self.comp_sockets = {}  # Socket of the destination components (linked with self with couplings)
        self.couplings = {}  # self_port: [(dest_comp, dest_port), ...]
        self.host = None
        self.port = None
        self._parse_xml(model_xml, model_name)

    def _parse_xml(self, model_xml: str, model_name: str):
        root = etree.parse(model_xml).getroot()
        self.host = root.attrib["host"]
        self.port = int(root.attrib["mainPort"])

        # This asumes that there is no coupled children
        for comp in root.getchildren()[::-1]:
            if comp.tag == "connection":
                if comp.attrib["componentFrom"] == model_name:
                    self.comp_sockets[comp.attrib["componentTo"]] = None

                    if comp.attrib["portFrom"] not in self.couplings:
                        self.couplings[comp.attrib["portFrom"]] = []

                    self.couplings[comp.attrib["portFrom"]].append((comp.attrib["componentTo"], comp.attrib["portTo"]))
            elif comp.tag == "atomic":
                if comp.attrib["name"] == model_name:
                    atomic_class = DistributedSimulator._get_external_class(comp.attrib["class"])
                    constructor_args = [model_name]

                    for arg in comp.getchildren():
                        if arg.tag == "constructor-arg":
                            constructor_args.append(DistributedSimulator._cast_arg(arg.attrib["value"]))

                    print("Loading class %s with the following args:" % comp.attrib["class"], constructor_args)
                    super().__init__(atomic_class(*constructor_args), SimulationClock())

                elif comp.attrib["name"] in self.comp_sockets:
                    self.comp_sockets[comp.attrib["name"]] = (comp.attrib["host"], int(comp.attrib["mainPort"]))

        print(self.comp_sockets, self.couplings)

    @staticmethod
    def _get_external_class(name: str):
        components = name.split('.')
        mod = __import__(components[0])
        for comp in components[1:]:
            mod = getattr(mod, comp)
        return mod

    @staticmethod
    def _cast_arg(val: str):
        if val.replace('.', '', 1).isdigit():
            val_f = float(val)
            val_i = int(val_f)
            return val_i if val_i == val_f else val_f
        else:
            return val

    @staticmethod
    def _cast_port_values(values):
        res = []
        for val_type, val_attribs in values:
            val_class = DistributedSimulator._get_external_class(val_type)
            curr = val_class(**val_attribs)

            for k, v in val_attribs.items():
                setattr(curr, k, v)

            res.append(curr)

        return res

    @staticmethod
    def _encode_port_values(values):
        res = []
        for val in values:
            val_cl = val.__class__
            val_type = ".".join([val_cl.__module__, val_cl.__name__])
            val_attribs = val.__dict__
            res.append([val_type, val_attribs])

        return res


    def _propagate_output(self):
        for out_port in self.model.out_ports:
            if not out_port.empty():
                if not out_port.name in self.couplings:
                    raise RuntimeError("Non-empty output port without couplings.")
                dest_comps = self.couplings[out_port.name]
                for dest_comp, dest_port in dest_comps:
                    msg = {"cmd": "inject", "port": "i_in"}
                    msg["values"] = DistributedSimulator._encode_port_values(out_port.values)

                    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                        dest_host, dest_port = self.comp_sockets[dest_comp]
                        print("Connecting to %s:%d..." % (dest_host, dest_port))
                        # s.connect((dest_host, dest_port))
                        print("Sending data: ", json.dumps(msg).encode())
                        # s.sendall(json.dumps(msg).encode())

    def _interpret_msg(self, msg_str):
        print("Interpreting...", msg_str)
        res = {}

        try:
            msg = json.loads(msg_str)
        except json.decoder.JSONDecodeError:
            msg = None
            res["error"] = DistributedSimulator.ERR_INVALID_MSG

        if msg is not None:
            if "cmd" in msg:
                if "time" in msg:
                    self.clock.time = msg["time"]

                if msg["cmd"] == DistributedSimulator.CMD_INITIALIZE:
                    print("Initializing")
                    self.model.initialize()

                elif msg["cmd"] == DistributedSimulator.CMD_TA:
                    print("ta")
                    res[DistributedSimulator.CMD_TA] = self.model.ta

                elif msg["cmd"] == DistributedSimulator.CMD_LAMBDA:
                    print("Lambda")
                    self.model.lambdaf()

                elif msg["cmd"] == DistributedSimulator.CMD_INJECT:
                    print("Inject")
                    if "port" in msg and "values" in msg:
                        inject_port = msg["port"]
                        found = False
                        for port in itertools.chain(self.model.in_ports, self.model.out_ports):
                            if port.name == inject_port:
                                port.extend(DistributedSimulator._cast_port_values(msg["values"]))
                                found = True
                                break
                        if not found:
                            res["error"] = DistributedSimulator.ERR_INVALID_PORT
                    else:
                        res["error"] = DistributedSimulator.ERR_BAD_CMD

                elif msg["cmd"] == DistributedSimulator.CMD_PROPAGATE_OUT:
                    print("Propagate output")
                    self._propagate_output()

                elif msg["cmd"] == DistributedSimulator.CMD_DELTFCN:
                    print("Deltfcn")
                    self.deltfcn()

                elif msg["cmd"] == DistributedSimulator.CMD_CLEAR:
                    print("Clear")
                    self.clear()

                elif msg["cmd"] == DistributedSimulator.CMD_EXIT:
                    print("Exit")
                    self.exit()
                    res["status"] = DistributedSimulator.STATUS_EXIT

                else:
                    res["error"] = DistributedSimulator.ERR_BAD_CMD
            else:
                res["error"] = DistributedSimulator.ERR_NO_CMD

        if "status" not in res:
            if "error" in res:
                res["status"] = DistributedSimulator.STATUS_ERR
            else:
                res["status"] = DistributedSimulator.STATUS_OK

        return res

    def listen(self):
        print("Listening on %s:%d..." % (self.host, self.port))
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((self.host, self.port))
            s.listen()
            conn, addr = s.accept()
            # s.setblocking(False)

            with conn:
                print('Connected by', addr)
                data = ""

                while True:
                    buff = conn.recv(self.buff_size)
                    buff = buff.decode("utf-8")
                    data += buff

                    lb_ind = buff.find('\n')
                    if lb_ind != -1:
                        res = self._interpret_msg(data[:len(data) + lb_ind + 1].strip())
                        conn.sendall((json.dumps(res) + "\n").encode())
                        data = data[len(data)+lb_ind+1:]
                        print(res)
                        if res["status"] == DistributedSimulator.STATUS_EXIT:
                            break
