import pickle
import logging
from itertools import chain
from abc import ABC, abstractmethod
from collections import deque, defaultdict

from . import PHASE_ACTIVE, PHASE_PASSIVE, INFINITY

logging.basicConfig(level=logging.DEBUG)


class Port:
    def __init__(self, p_type=None, name: str = None, serve: bool = False):
        self.name = name if name else self.__class__.__name__
        self.parent = None
        self.values = deque()
        self.p_type = p_type
        self.serve = serve
        self.conn_ports = list()      # List of ports that inject data to the port

    def __bool__(self):
        return bool(self.values) or any(self.conn_ports)

    def __len__(self):
        return len(self.values) + sum([len(port) for port in self.conn_ports])

    def __str__(self):
        return "{Port(%s): [%s]}" % (self.p_type, self.values)

    def empty(self):
        return not bool(self) and all([not port for port in self.conn_ports])

    def clear(self):
        self.values.clear()
        for port in self.conn_ports:    # TODO is this OK?
            port.clear()

    def get(self):
        try:
            return self.values[0]
        except IndexError:
            for port in self.conn_ports:
                try:
                    return port.get()
                except IndexError:
                    pass
        raise IndexError

    def get_all(self):
        return chain(self.values, *[port.get_all() for port in self.conn_ports])

    def add(self, val):
        if type and not isinstance(val, self.p_type):
            raise TypeError("Value type is %s (%s expected)" % (type(val).__name__, self.p_type.__name__))
        self.values.append(val)

    def extend(self, vals):
        for val in vals:
            self.add(val)


class Component(ABC):
    def __init__(self, name: str = None):
        self.name = name if name else self.__class__.__name__
        self.parent = None
        self.in_ports = list()
        self.out_ports = list()

    def __str__(self):
        in_str = " ".join([p.name for p in self.in_ports])
        out_str = " ".join([p.name for p in self.out_ports])
        return "%s: InPorts[%s] OutPorts[%s]" % (self.name, in_str, out_str)

    @abstractmethod
    def initialize(self):
        pass

    @abstractmethod
    def exit(self):
        pass

    def in_empty(self):
        return all([p.empty() for p in self.in_ports])

    def out_empty(self):
        return all([p.empty() for p in self.out_ports])

    def add_in_port(self, port):
        port.parent = self
        self.in_ports.append(port)

    def add_out_port(self, port):
        port.parent = self
        self.out_ports.append(port)


class Coupling:
    def __init__(self, port_from: Port, port_to: Port, host=None):
        self.port_from = port_from
        self.port_to = port_to
        self.host = host

    def __str__(self):
        return "(%s -> %s)" % (self.port_from, self.port_to)

    def propagate(self):
        if self.host:
            if len(self.port_from) > 0:
                values = list(map(lambda x: pickle.dumps(x, protocol=0).decode(), self.port_from.get_all()))
                try:
                    self.host.inject(self.port_to, values)
                except:
                    logging.warning("Values could not be injected (%s)" % self.port_to)
        else:
            self.port_to.extend(self.port_from.get_all())


class Atomic(Component):
    def __init__(self, name: str = None):
        self.name = name if name else self.__class__.__name__
        super().__init__(name)

        self.phase = PHASE_PASSIVE
        self.sigma = INFINITY

    @property
    def ta(self):
        return self.sigma

    def __str__(self):
        return "%s(%s, %s)" % (self.name, self.phase, self.sigma)

    @abstractmethod
    def deltint(self):
        pass

    @abstractmethod
    def deltext(self, e):
        pass

    @abstractmethod
    def lambdaf(self):
        pass

    def deltcon(self, e):
        self.deltint()
        self.deltext(0)

    def hold_in(self, phase, sigma):
        self.phase = phase
        self.sigma = sigma

    def activate(self):
        self.phase = PHASE_ACTIVE
        self.sigma = 0

    def passivate(self, phase=PHASE_PASSIVE):
        self.phase = phase
        self.sigma = INFINITY


class Coupled(Component):
    def __init__(self, name: str = None, perf: bool = True):
        self.name = name if name else self.__class__.__name__
        self.perf = perf
        super().__init__(name)

        self.components = []
        self.ic = []
        self.eic = []
        self.eoc = []

    def initialize(self):
        pass

    def exit(self):
        pass

    def add_coupling(self, p_from: Port, p_to: Port, host=None):
        if self.perf:
            p_to.conn_ports.append(p_from)
        else:
            coupling = Coupling(p_from, p_to, host)
            if p_from.parent == self:
                self.eic.append(coupling)
            elif p_to.parent == self:
                self.eoc.append(coupling)
            else:
                self.ic.append(coupling)

    def add_component(self, component):
        component.parent = self
        self.components.append(component)

    def flatten(self):
        for comp in self.components:
            if isinstance(comp, Coupled):
                comp.flatten()
                self._remove_couplings(comp)
                self.components.remove(comp)

        if self.parent is None:
            return self

        left_bridge_eic = self._create_left_bridge(self.parent.eic)
        left_bridge_ic = self._create_left_bridge(self.parent.ic)
        right_bridge_eic = self._create_right_bridge(self.parent.eoc)
        right_bridge_ic = self._create_right_bridge(self.parent.ic)

        self._complete_left_bridge(left_bridge_eic, self.parent.eic)
        self._complete_left_bridge(left_bridge_ic, self.parent.ic)
        self._complete_right_bridge(right_bridge_eic, self.parent.eoc)
        self._complete_right_bridge(right_bridge_ic, self.parent.ic)

        for comp in self.components:
            self.parent.add_component(comp)

        for coup in self.ic:
            self.parent.ic.append(coup)

        return self

    def _remove_couplings(self, child):
        in_ports = child.in_ports

        for in_port in in_ports:
            for coup in self.eic:
                if coup.port_to == in_port:
                    self.eic.remove(coup)

            for coup in self.ic:
                if coup.port_to == in_port:
                    self.ic.remove(coup)

        for out_port in self.out_ports:
            for coup in self.eoc:
                if coup.port_to == out_port:
                    self.eoc.remove(coup)

            for coup in self.ic:
                if coup.port_to == out_port:
                    self.ic.remove(coup)

    def _create_left_bridge(self, pc):
        bridge = defaultdict(list)

        for in_port in self.in_ports:
            for coup in pc:
                if coup.port_to == in_port:
                    bridge[in_port].append(coup.port_from)
        return bridge

    def _create_right_bridge(self, pc):
        bridge = defaultdict(list)

        for out_port in self.out_ports:
            for coup in pc:
                if coup.port_from == out_port:
                    bridge[out_port].append(coup.port_to)
        return bridge

    def _complete_left_bridge(self, bridge, pc):
        for coup in self.eic:
            ports = bridge[coup.port_from]
            for port in ports:
                pc.append(Coupling(port, coup.port_to))

    def _complete_right_bridge(self, bridge, pc):
        for coup in self.eoc:
            ports = bridge[coup.port_to]
            for port in ports:
                pc.append(Coupling(coup.port_from, port))
