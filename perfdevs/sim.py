import _thread
import pickle
from abc import ABC, abstractmethod
from xmlrpc.server import SimpleXMLRPCServer

from concurrent import futures

from . import INFINITY, get_logger
from .models import Atomic, Coupled

logger = get_logger(__name__)


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
        t = self.clock.time
        in_empty = self.model.in_empty()

        if in_empty:
            if t != self.time_next:
                return
            self.model.deltint()
        else:
            e = t - self.time_last
            self.model.sigma -= e

            if t == self.time_next:
                self.model.deltcon(e)
            else:
                self.model.deltext(e)

        self.time_last = t
        self.time_next = self.time_last + self.model.ta

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
        return min([proc.time_next for proc in self.processors], default=0) - self.clock.time

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
        logger.debug("INJECTING")
        time = self.time_last + e

        if type(values) is not list:
            values = [values]

        if type(port) is str:
            values = list(map(lambda x: pickle.loads(x.encode()), values))
            if port in self.ports_to_serve:
                port = self.ports_to_serve[port]
            else:
                logger.error("Port '%s' not found" % port)
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
            logger.error("Time %d - Input rejected: elapsed time %d is not in bounds" % (self.time_last, e))
            return False

    def simulate(self, num_iters=10000):
        logger.debug("STARTING SIMULATION...")
        self.clock.time = self.time_next

        cont = 0
        while cont < num_iters and self.clock.time < INFINITY:
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next
            cont += 1

    def simulate_time(self, time_interv=10000):
        logger.debug("STARTING SIMULATION...")
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
                 unroll: bool = True, executor: futures.ProcessPoolExecutor = None):
        super().__init__(model, clock, flatten, chain, unroll)

        if executor is None:
            executor = futures.ThreadPoolExecutor(4)
        self.executor = executor

    def _add_coordinator(self, coupled: Coupled):
        coord = ParallelCoordinator(coupled, self.clock)
        self.coordinators.append(coord)
        self.ports_to_serve.update(coord.ports_to_serve)

    def lambdaf(self):
        for coord in self.coordinators:
            coord.lambdaf()
        ex_futures = []
        for sim in self.simulators:
            ex_futures.append(self.executor.submit(sim.lambdaf))

        for future in futures.as_completed(ex_futures):
            future.result()

        self.propagate_output()

    def deltfcn(self):
        self.propagate_input()

        for coord in self.coordinators:
            coord.deltfcn()
        ex_futures = []
        for sim in self.simulators:
            ex_futures.append(self.executor.submit(sim.deltfcn))

        for future in futures.as_completed(ex_futures):
            future.result()

        self.time_last = self.clock.time
        self.time_next = self.time_last + self.ta()
