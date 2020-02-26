import _thread
import logging
import pickle
import heapq
from abc import ABC, abstractmethod
from xmlrpc.server import SimpleXMLRPCServer

from . import INFINITY, DEBUG
from .models import Atomic, Coupled

if DEBUG:
    logging.basicConfig(level=logging.DEBUG)
else:
    logging.disable(logging.CRITICAL)


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
    EV_NONE = 0
    EV_INT = 1
    EV_EXT = 2
    EV_CON = 3

    def __init__(self, model: Atomic, clock: SimulationClock, handling):
        super().__init__(clock)
        self.model = model
        self.handling = handling
        self.event = Simulator.EV_NONE
        self.time_last = self.clock.time
        self.time_next = self.clock.time

    def __lt__(self, other):
        return True

    @property
    def ta(self):
        return self.model.sigma

    def initialize(self):
        self.model.initialize()
        #self.time_next = self.time_last + self.model.ta

        if self.ta != INFINITY:
            # self.time_next = self.time_last + self.model.ta
            self.time_next = self.clock.time + self.model.ta
            self.handling.add_lambda_event(self.time_next, self)

    def exit(self):
        self.model.exit()

    """def deltfcn(self):
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
        self.time_next = self.time_last + self.model.ta"""

    def deltfcn(self):
        t = self.clock.time
        last_ta = self.model.sigma

        if last_ta == INFINITY:  # It's not included in lambdas queue and corresponds to an external event
            e = t - self.time_last
            # self.model.sigma -= e  # It has no sense to decrement inf

            self.model.deltext(e)

            if self.model.sigma != INFINITY:
                #self.time_next = t + self.model.sigma
                self.handling.lambda_q.put((self.time_next, self))

        else:
            if self.event == Simulator.EV_INT:  # It's not included in lambdas queue
                self.model.deltint()
                self.time_next = t + self.model.sigma

                if self.model.sigma != INFINITY:
                    self.handling.add_lambda_event(self.time_next, self)
            else:
                e = t - self.time_last
                self.model.sigma -= e

                if self.event == Simulator.EV_EXT:
                    self.model.deltext(e)

                    if self.model.sigma == INFINITY:
                        self.time_next = INFINITY
                        self.handling.remove_lambdaf_sim(self)
                    elif last_ta != self.model.sigma:
                        self.time_next = t + self.model.sigma
                        self.handling.remove_lambdaf_sim(self)
                        self.handling.add_lambda_event(self.time_next, self)

                else:  # EV_CON
                    self.model.deltcon(e)
                    self.time_next = t + self.model.sigma

                    if self.model.sigma != INFINITY:
                        self.handling.add_lambda_event(self.time_next, self)

        self.time_last = t

    def lambdaf(self):
        assert self.clock.time == self.time_next

        self.model.lambdaf()
        for port in self.model.out_ports:
            if not port.empty():
                self.handling.prop_out.add(port)

        self.add_int_event()

    def add_int_event(self):
        if self.event == Simulator.EV_EXT:
            self.event = Simulator.EV_CON
        elif self.event != Simulator.EV_CON:
            self.event = Simulator.EV_INT

    def add_ext_event(self):
        if self.event == Simulator.EV_INT:
            self.event = Simulator.EV_CON
        elif self.event != Simulator.EV_CON:
            self.event = Simulator.EV_EXT

    def clear(self):
        for in_port in self.model.in_ports:
            in_port.clear()

        for out_port in self.model.out_ports:
            out_port.clear()


class Coordinator(AbstractSimulator):

    class Handling:
        def __init__(self):
            self.lambda_q = []  # binary heap
            self.sims_with_events_s = set()  # set of models with at least one event (int, ext, con)
            self.prop_in = set()  # prop_in and prop_out stores the ports that have to be propagated
            self.prop_out = set()
            self.simulators = dict()  # look-up tables "atomic -> simulator"
            self.coordinators = dict()  # look-up tables "coupled -> coupled"

        def add_lambda_event(self, time, sim):
            heapq.heappush(self.lambda_q, (time, sim))

        def pop_lambda_event(self):
            return heapq.heappop(self.lambda_q)

        def remove_lambda_event(self, sim):
            """
            This element removal is O(n). It could be improved using heapq internal methods to O(log(n))
            See https://stackoverflow.com/questions/10162679/python-delete-element-from-heap
            TODO evaluate convenience of the change

            h[i] = h[-1]
            h.pop()
            if i < len(h):
                heapq._siftup(h, i)
                heapq._siftdown(h, 0, i)
            """
            for ind, pair in enumerate(self.lambda_q):
                if pair[1] == sim:
                    self.lambda_q[ind] = self.lambda_q[-1]
                    self.lambda_q[ind].pop()
                    heapq.heapify(self.lambda_q[ind])
                    return

    def __init__(self, model: Coupled, clock: SimulationClock = None, handling: Handling = None,
                 flatten: bool = False, force_chain: bool = False, split_chain: bool = False):
        super().__init__(clock or SimulationClock())

        self.coordinators = []
        self.simulators = []
        self.model = model
        self.model.chain_components(force_chain, split_chain)
        if flatten:
            self.model.flatten()
        self.ports_to_serve = dict()
        self.handling = handling or Coordinator.Handling()

    @property
    def time_next(self):
        return self.handling.lambda_q[0][0] if self.handling.lambda_q else INFINITY

    @time_next.setter
    def time_next(self, value):
        pass

    def initialize(self):
        self._build_hierarchy()

        for sim in self.simulators:
            sim.initialize()

        for coord in self.coordinators:
            coord.initialize()

        self.time_last = self.clock.time

    def _build_hierarchy(self):
        for comp in self.model.components:
            if isinstance(comp, Coupled):
                self.__add_coordinator(comp)
            elif isinstance(comp, Atomic):
                self.__add_simulator(comp)

    def __add_coordinator(self, coupled: Coupled):
        coord = Coordinator(coupled, self.clock, self.handling)
        self.handling.coordinators[coupled] = coord
        self.coordinators.append(coord)
        self.ports_to_serve.update(coord.ports_to_serve)

    def __add_simulator(self, atomic: Atomic):
        sim = Simulator(atomic, self.clock, self.handling)
        self.handling.simulators[atomic] = sim
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
        for sim in self.simulators:
            sim.exit()

        for coord in self.coordinators:
            coord.exit()

    def ta(self):
        if self.handling.lambda_q:
            return self.handling.lambda_q[0][0] - self.clock.time
        else:
            return INFINITY

    def lambdaf(self):
        self.clock.time, sim = self.handling.pop_lambda_event()
        sim.lambdaf()
        self.handling.sims_with_events_s.add(sim)

        while self.handling.lambda_q and self.handling.lambda_q[0][0] == self.clock.time:
            _, sim = self.handling.pop_lambda_event()
            sim.lambdaf()
            self.handling.sims_with_events_s.add(sim)

        self.propagate_output()

    def propagate_output(self):
        """
        for coup in self.model.ic:
            coup.propagate()

        for coup in self.model.eoc:
            coup.propagate()

        for coord in self.coordinators.values():
            coord.propagate_output()"""

        while self.handling.prop_out:
            src_port = self.handling.prop_out.pop()

            # TODO: check this condition
            if src_port.parent.link or (isinstance(src_port.parent, Coupled) and src_port.parent.chain):
                raise NotImplementedError()
            else:
                if src_port in self.model.ic:
                    for coup in self.model.ic[src_port]:
                        coup.propagate()
                        if isinstance(coup.port_to.parent, Atomic):
                            sim = self.handling.simulators[coup.port_to.parent]
                            sim.add_ext_event()
                            self.handling.sims_with_events_s.add(sim)
                        else:
                            self.handling.prop_in.add(coup.port_to)

                if src_port in self.model.eoc:
                    for coup in self.model.eoc[src_port]:
                        coup.propagate()
                        self.handling.prop_out.add(coup.port_to)

    def deltfcn(self):
        self.propagate_input()

        while self.handling.sims_with_events_s:
            self.handling.sims_with_events_s.pop().deltfcn()

        self.time_last = self.clock.time
        # self.time_next = self.time_last + self.ta()

    def propagate_input(self, src_port=None):

        if src_port:
            if src_port in self.model.eic:
                for coup in self.model.eic[src_port]:
                    coup.propagate()
                    if isinstance(coup.port_to.parent, Atomic):
                        sim = self.handling.simulators[coup.port_to.parent]
                        sim.add_ext_event()
                        self.handling.sims_with_events_s.add(sim)
                    else:
                        self.handling.coordinators[coup.port_to.parent].propagate_input(coup.port_to)
        else:
            if self.model.chain:
                raise NotImplementedError()
            else:
                while self.handling.prop_in:
                    port = self.handling.prop_in.pop()
                    self.handling.coordinators[port.parent].propagate_input(port)

    def clear(self):
        for sim in self.simulators:
            sim.clear()

        for coord in self.coordinators:
            coord.clear()

        for in_port in self.model.in_ports:
            in_port.clear()

        for out_port in self.model.out_ports:
            out_port.clear()

    def inject(self, port, values, e=0):
        logging.debug("INJECTING")
        time = self.time_last + e

        if type(values) is not list:
            values = [values]

        if type(port) is str:
            values = list(map(lambda x: pickle.loads(x.encode()), values))
            if port in self.ports_to_serve:
                port = self.ports_to_serve[port]
            else:
                logging.error("Port '%s' not found" % port)
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
            logging.error("Time %d - Input rejected: elapsed time %d is not in bounds" % (self.time_last, e))
            return False

    def simulate(self, num_iters=10000):
        logging.debug("STARTING SIMULATION...")
        self.clock.time = self.time_next

        cont = 0
        while cont < num_iters and self.clock.time < INFINITY:
            self.lambdaf()
            self.deltfcn()
            self.clear()
            self.clock.time = self.time_next
            cont += 1

    def simulate_time(self, time_interv=10000):
        logging.debug("STARTING SIMULATION...")
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
