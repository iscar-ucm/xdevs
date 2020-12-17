import _thread
import pickle
from abc import ABC, abstractmethod
from xmlrpc.server import SimpleXMLRPCServer

from concurrent import futures

from . import INFINITY, get_logger
from .models import Atomic, Coupled

#logger = get_#logger(__name__)


class SimulationClock:
    def __init__(self, time: float = 0):
        self.time = time


class AbstractSimulator(ABC):
    def __init__(self, clock: SimulationClock):
        self.clock = clock
        self.time_last = 0
        self.time_next = 0

    @abstractmethod
    def initialize(self):
        pass

    @abstractmethod
    def exit(self):
        pass

    @abstractmethod
    def ta(self):
        pass

    @abstractmethod
    def lambdaf(self):
        pass

    @abstractmethod
    def deltfcn(self):
        pass

    @abstractmethod
    def clear(self):
        pass


class Simulator(AbstractSimulator):
    def __init__(self, model: Atomic, clock: SimulationClock):
        super().__init__(clock)
        self.model = model

    @property
    def ta(self):
        return self.model.ta

    def initialize(self):
        self.model.initialize()
        self.time_last = self.clock.time
        self.time_next = self.time_last + self.model.ta

    def exit(self):
        self.model.exit()

    def deltfcn(self):
        #logger.debug("Deltfcn %s (empty: %s, t: %d)" % (self.model.name, self.model.in_empty(), self.clock.time))
        t = self.clock.time
        in_empty = self.model.in_empty()

        if in_empty:
            if t != self.time_next:
                return
            self.model.deltint()
        else:
            e = t - self.time_last

            if t == self.time_next:
                self.model.deltcon(e)
            else:
                self.model.deltext(e)

        self.time_last = t
        self.time_next = self.time_last + self.model.ta
        ##logger.debug("Deltfcn %s: TL: %s, TN: %s" % (self.model.name, self.time_last, self.time_next))
        return self

    def lambdaf(self):
        if self.clock.time == self.time_next:
            self.model.lambdaf()

    def clear(self):
        for in_port in self.model.in_ports:
            in_port.clear()

        for out_port in self.model.out_ports:
            out_port.clear()


class Coordinator(AbstractSimulator):
    def __init__(self, model: Coupled, clock: SimulationClock = None,
                 flatten: bool = False, chain: bool = False, unroll: bool = True):
        super().__init__(clock or SimulationClock())

        self.coordinators = []
        self.simulators = []
        self.model = model
        if chain:
            if not unroll:
                raise NotImplementedError
            self.model.chain_components(unroll or flatten)
        elif flatten:
            self.model.flatten()
        self.ports_to_serve = dict()

    @property
    def processors(self):
        for coord in self.coordinators:
            yield coord
        for sim in self.simulators:
            yield sim

    def initialize(self):
        self._build_hierarchy()

        for proc in self.processors:
            proc.initialize()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()

    def _build_hierarchy(self):
        for comp in self.model.components:
            if isinstance(comp, Coupled):
                self._add_coordinator(comp)
            elif isinstance(comp, Atomic):
                self._add_simulator(comp)

    def _add_coordinator(self, coupled: Coupled):
        coord = Coordinator(coupled, self.clock)
        self.coordinators.append(coord)
        self.ports_to_serve.update(coord.ports_to_serve)

    def _add_simulator(self, atomic: Atomic):
        sim = Simulator(atomic, self.clock)
        self.simulators.append(sim)
        for pts in sim.model.in_ports:
            if pts.serve:
                port_name = "%s.%s" % (pts.parent.name, pts.name)
                self.ports_to_serve[port_name] = pts

    def serve(self, host="localhost", port=8000):
        server = SimpleXMLRPCServer((host, port))
        server.register_function(self.inject)
        _thread.start_new_thread(server.serve_forever, ())

    def exit(self):
        for proc in self.processors:
            proc.exit()

    def ta(self):
        return min([proc.time_next for proc in self.processors], default=INFINITY) - self.clock.time

    def lambdaf(self):
        for proc in self.processors:
            proc.lambdaf()

        self.propagate_output()

    def propagate_output(self):
        if not self.model.chain:
            for _, coups in self.model.ic.items():
                for coup in coups:
                    coup.propagate()

            for _, coups in self.model.eoc.items():
                for coup in coups:
                    coup.propagate()

    def deltfcn(self):
        self.propagate_input()

        for proc in self.processors:
            proc.deltfcn()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()

    def propagate_input(self):
        if not self.model.chain:
            for _, coups in self.model.eic.items():
                for coup in coups:
                    coup.propagate()

    def clear(self):
        for proc in self.processors:
            proc.clear()

        for in_port in self.model.in_ports:
            in_port.clear()

        for out_port in self.model.out_ports:
            out_port.clear()

    def inject(self, port, values, e=0):
        #logger.debug("INJECTING")
        time = self.time_last + e

        if type(values) is not list:
            values = [values]

        if type(port) is str:
            values = list(map(lambda x: pickle.loads(x.encode()), values))
            if port in self.ports_to_serve:
                port = self.ports_to_serve[port]
            else:
                #logger.error("Port '%s' not found" % port)
                return True

        if time <= self.time_next or time != time:
            port.extend(values)
            self.clock.time = time
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next
            return True
        else:
            #logger.error("Time %d - Input rejected: elapsed time %d is not in bounds" % (self.time_last, e))
            return False

    def simulate(self, num_iters=10000):
        #logger.debug("STARTING SIMULATION...")
        self.clock.time = self.time_next

        cont = 0
        while cont < num_iters and self.clock.time < INFINITY:
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next
            cont += 1

    def simulate_time(self, time_interv=10000):
        #logger.debug("STARTING SIMULATION...")
        self.clock.time = self.time_next
        tf = self.clock.time + time_interv

        while self.clock.time < tf:
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next

    def simulate_inf(self):

        while True:
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next


class ParallelCoordinator(Coordinator):

    def __init__(self, model: Coupled, clock: SimulationClock = None, flatten: bool = False, chain: bool = False,
                 unroll: bool = True, executor=None):
        super().__init__(model, clock, flatten, chain, unroll)

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

    def __init__(self, model: Coupled, clock: SimulationClock = None, flatten: bool = False, chain: bool = False,
                 master=True, executor=None, executor_futures=None):
        super().__init__(model, clock, flatten, chain)
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
                #logger.debug("D: Waiting... (%d/%d)" % (i+1, len(self.executor_futures)))
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
                #logger.debug("D: Waiting... (%d/%d)" % (i+1, len(self.executor_futures)))
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
        #logger.debug({proc.model.name:proc.time_next for proc in self.processors})
        #logger.debug("Deltfcn %s: TL: %s, TN: %s" % (self.model.name, self.time_last, self.time_next))
