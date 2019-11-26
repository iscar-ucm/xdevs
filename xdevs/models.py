from xdevs import PHASE_ACTIVE, PHASE_PASSIVE, INFINITY

from abc import ABC, abstractmethod
from collections import deque, defaultdict
import pickle
import logging

logging.basicConfig(level=logging.DEBUG)

class Port:
    def __init__(self, p_type=None, name=None, serve=False):
        self.name = name if name else self.__class__.__name__
        self.parent = None
        self.values = deque()
        self.p_type = p_type
        self.serve = serve

    def __bool__(self):
        return bool(self.values)

    def __len__(self):
        return len(self.values)

    def __str__(self):
        return "{Port(%s): [%s]}" % (self.p_type, self.values)

    def empty(self):
        return not bool(self)

    def clear(self):
        self.values.clear()

    def get(self):
        return self.values[0]

    def add(self, val):
        if type and not isinstance(val, self.p_type):
            raise TypeError("Value type is %s (%s expected)" % (type(val).__name__, self.p_type.__name__))
        self.values.append(val)

    def extend(self, vals):
        for val in vals:
            self.add(val)


class Component(ABC):
    def __init__(self, name=None):
        self.name = name if name else self.__class__.__name__
        self.parent = None
        self.in_ports = []
        self.out_ports = []

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
    def __init__(self, port_from, port_to, host=None):
        self.port_from = port_from
        self.port_to = port_to
        self.host = host

    def __str__(self):
        return "(%s -> %s)" % (self.port_from, self.port_to)

    def propagate(self):
        if self.host:
            if len(self.port_from.values) > 0:
                values = list(map(lambda x: pickle.dumps(x, protocol=0).decode(), self.port_from.values))
                try:
                    self.host.inject(self.port_to, values)
                except:
                    logging.warning("Values could not be injected (%s)" % self.port_to)
        else:
            self.port_to.extend(self.port_from.values)


class Atomic(Component):
    def __init__(self, name=None):
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
    def __init__(self, name=None):
        self.name = name if name else self.__class__.__name__
        super().__init__(name)

        self.components = []
        self.ic = []
        self.eic = []
        self.eoc = []

    def initialize(self):
        pass

    def exit(self):
        pass

    def add_coupling(self, c_from, i_from, c_to, i_to):
        """
		This method add a connection to the DEVS component.
		
		:param Component c_from Component at the beginning of the connection
		:param int i_from Index of the source port in c_from, starting at 0
		:param Component c_to Component at the end of the connection
		:param int i_to Index of the destination port in c_to, starting at 0
		"""

        ports_from = c_from.in_ports if c_from == self else c_from.out_ports
        ports_to = c_to.out_ports if c_to == self else c_to.in_ports

        return self.add_coupling(ports_from[i_from], ports_to[i_to])

    def add_coupling(self, p_from, p_to, host=None):
        coupling = Coupling(p_from, p_to, host)

        if p_from.parent == self:
            self.eic.append(coupling)
        elif type(p_to) is Port and p_to.parent == self:
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
