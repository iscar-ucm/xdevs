import pickle
import logging
from itertools import chain
from abc import ABC, abstractmethod
from collections import deque, defaultdict
from typing import Any, Iterator, Tuple, List

from . import PHASE_ACTIVE, PHASE_PASSIVE, INFINITY

logging.basicConfig(level=logging.DEBUG)

PORT_IN = "in"
PORT_OUT = "out"


class Port:
    def __init__(self, p_type=None, name: str = None, serve: bool = False):
        """
        xDEVS implementation of DEVS Port.
        :param p_type: data type of messages to be sent/received via the new port instance.
        :param name: name of the new port instance.
        :param serve: set to True if the port is going to be accessible via RPC server.
        """
        self.name = name if name else self.__class__.__name__
        self.parent = None
        self.direction = None           # added direction to ports
        self.values = deque()
        self.p_type = p_type
        self.serve = serve
        self.couplings_to = list()      # List of ports that receive data from the port (only used by output ports)
        self.couplings_from = list()    # List of ports that inject data to the port (only used by input ports)

    def __bool__(self) -> bool:
        """:return: True if port contains any message."""
        return bool(self.values) or any(self.couplings_from)

    def __len__(self) -> int:
        """:return: number of messages contained in the port."""
        return len(self.values) + sum([len(port) for port in self.couplings_from])

    def __str__(self) -> str:
        """:return: stringified representation of the port."""
        return "{Port(%s): [%s]}" % (self.p_type, list(self.get_all()))

    def empty(self) -> bool:
        """:return: True if no messages are contained in the port."""
        return not bool(self) and all([not port for port in self.couplings_from])

    def clear(self):
        """Removes messages contained in the port."""
        self.values.clear()  # TODO clear all of the linked ports as well?

    def get(self) -> Any:
        """
        :return: ONE single message from port.
            By default, it will return a value contained in its own values bag.
            If no message is found in its own bag, it will iterate over chained ports looking for a message.
        :raises IndexError: if port is empty.
        """
        try:
            return self.values[0]
        except IndexError:
            for port in self.couplings_from:
                try:
                    return port.get()
                except IndexError:
                    continue
        raise IndexError

    def get_all(self) -> Iterator[Any]:
        """:return: iterator containing all the messages in the port."""
        return chain(self.values, *[port.get_all() for port in self.couplings_from])

    def add(self, val: Any):
        """
        Adds a new value to the local message bag of the port.
        :param val: message to be added.
        :raises TypeError: If message is not instance of port type.
        """
        if type and not isinstance(val, self.p_type):
            raise TypeError("Value type is %s (%s expected)" % (type(val).__name__, self.p_type.__name__))
        self.values.append(val)

    def extend(self, vals: Iterator[Any]):
        """
        Adds a set of new values to the local message bag of the port.
        :param vals: list containing all the messages to be added.
        :raises TypeError: If one of the messages is not instance of port type.
        """
        for val in vals:
            self.add(val)


class Component(ABC):
    def __init__(self, name: str = None):
        """
        Abstract Base Class for an xDEVS model.
        :param name: name of the xDEVS model. If no name is provided, it will take the class's name by default.
        """
        self.name = name if name else self.__class__.__name__
        self.chain = False  # True if component is a chain
        self.link = False   # True if component is a link of a chain
        self.parent = None
        self.in_ports = list()
        self.out_ports = list()

    def __str__(self) -> str:
        """:return: stringified version of a component"""
        in_str = " ".join([p.name for p in self.in_ports])
        out_str = " ".join([p.name for p in self.out_ports])
        return "%s: InPorts[%s] OutPorts[%s]" % (self.name, in_str, out_str)

    @abstractmethod
    def initialize(self):
        """Initializes the xDEVS model."""
        pass

    @abstractmethod
    def exit(self):
        """Exits the xDEVS model."""
        pass

    def in_empty(self) -> bool:
        """:return: True if model has not any message in all its input ports."""
        return all([p.empty() for p in self.in_ports])

    def out_empty(self) -> bool:
        """:return: True if model has not any message in all its output ports."""
        return all([p.empty() for p in self.out_ports])

    def add_in_port(self, port: Port):
        """
        Adds an input port to the xDEVS model.
        :param port: port to be added to the model.
        """
        port.parent = self
        port.direction = PORT_IN
        self.in_ports.append(port)

    def add_out_port(self, port: Port):
        """
        Adds an output port to the xDEVS model
        :param port: port to be added to the model.
        """
        port.parent = self
        port.direction = PORT_OUT
        self.out_ports.append(port)


class Coupling:
    def __init__(self, port_from: Port, port_to: Port, host=None):  # TODO identify host's variable type
        """
        xDEVS implementation of DEVS couplings.
        :param port_from: DEVS transmitter port.
        :param port_to: DEVS receiver port.
        :param host: TODO documentation for this
        :raises ValueError: if coupling direction is incompatible with the target ports.
        """
        # Check that couplings are valid
        comp_from = port_from.parent
        comp_to = port_to.parent
        if isinstance(comp_from, Atomic) and port_from.direction == PORT_IN:
            raise ValueError("Input ports whose parent is an Atomic model can not be coupled to any other port")
        if isinstance(comp_to, Atomic) and port_to.direction == PORT_OUT:
            raise ValueError("Output ports whose parent is an Atomic model can not be recipient of any other port")
        # TODO assert that port types are compatible
        self.port_from = port_from
        self.port_to = port_to
        self.host = host

    def __str__(self) -> str:
        """:return: stringified version of the coupling."""
        return "(%s -> %s)" % (self.port_from, self.port_to)

    def propagate(self):
        """Copies messages from the transmitter port to the receiver port"""
        if self.host:
            if len(self.port_from) > 0:
                values = list(map(lambda x: pickle.dumps(x, protocol=0).decode(), self.port_from.get_all()))
                try:
                    self.host.inject(self.port_to, values)
                except:  # TODO identify exception type
                    logging.warning("Values could not be injected (%s)" % self.port_to)
        else:
            self.port_to.extend(self.port_from.get_all())


class Atomic(Component, ABC):
    def __init__(self, name: str = None):
        """
        xDEVS implementation of DEVS Atomic Model.
        :param name: name of the Atomic Model. If no name is provided, it will take the class's name by default.
        """
        self.name = name if name else self.__class__.__name__
        super().__init__(name)

        self.phase = PHASE_PASSIVE
        self.sigma = INFINITY

    @property
    def ta(self) -> Any:
        """:return: remaining time for the atomic model's internal transition."""
        return self.sigma

    def __str__(self) -> str:
        """:return: stringified representation of atomic model."""
        return "%s(%s, %s)" % (self.name, self.phase, self.sigma)

    @abstractmethod
    def deltint(self):
        """Describes the internal transitions of the atomic model."""
        pass

    @abstractmethod
    def deltext(self, e: Any):
        """
        Describes the external transitions of the atomic model.
        :param e: elapsed time between last transition and the external transition.
        """
        pass

    @abstractmethod
    def lambdaf(self):
        """Describes the output function of the atomic model."""
        pass

    def deltcon(self, e: Any):
        """
        Describes the confluent transitions of the atomic model. By default, the internal transition is triggered first.
        :param e: elapsed time between last transition and the confluent transition.
        """
        self.deltint()
        self.deltext(0)

    def hold_in(self, phase: Any, sigma: Any):
        """
        Change atomic model's phase and next timeout.
        :param phase: atomic model's new phase.
        :param sigma: time remaining to the next timeout.
        """
        self.phase = phase
        self.sigma = sigma

    def activate(self):
        """Sets phase to PHASE_ACTIVE and next timeout to 0"""
        self.phase = PHASE_ACTIVE
        self.sigma = 0

    def passivate(self, phase=PHASE_PASSIVE):
        """Sets phase to PHASE_PASSIVE and next timeout to INFINITY"""
        self.phase = phase
        self.sigma = INFINITY


class Coupled(Component, ABC):
    def __init__(self, name: str = None, chain: bool = False):
        """
        xDEVS implementation of DEVS Coupled Model.
        :param name: name of the Atomic Model. If no name is provided, it will take the class's name by default.
        :param chain: set it to True to enable submodel chaining. By default, chaining is disabled.
        """
        self.name = name if name else self.__class__.__name__
        super().__init__(name)
        self.chain = chain

        self.components = []
        self.ic = []
        self.eic = []
        self.eoc = []

    def initialize(self):
        pass

    def exit(self):
        pass

    def add_coupling(self, p_from: Port, p_to: Port, host=None):
        """
        Adds coupling betweem two submodules of the coupled model.
        :param p_from: DEVS transmitter port.
        :param p_to: DEVS receiver port.
        :param host: TODO documentation
        :raises ValueError: if coupling is not well defined.
        """
        if self.chain and host is not None:
            raise ValueError("A chain cannot host couplings")
        coupling = Coupling(p_from, p_to, host)
        if p_from.parent == self and p_to.parent in self.components:
            self.eic.append(coupling)
        elif p_from.parent in self.components and p_to.parent == self:
            self.eoc.append(coupling)
        elif p_from.parent in self.components and p_to.parent in self.components:
            self.ic.append(coupling)
        else:
            raise ValueError("Components that compose the coupling are not submodules of coupled model")

    def add_component(self, component: Component):
        """
        Adds subcomponent to coupled model.
        :param component: component to be added to the Coupled model.
        """
        component.parent = self
        self.components.append(component)

    def chain_components(self, force: bool = False, split: bool = False) -> Tuple[List, List]:
        """
        Chains submodules to enhance sequential execution performance.
        :param force: if True, the entire hierarchy is chained. By default, it is set to False.
        :param split: if True, coupled models may be divided into an equivalent set of chained and unchained models.
        :return: True if chaining was triggered.
        """
        self.chain |= force
        for comp in self.components:
            comp.link = self.chain
            if isinstance(comp, Coupled):
                if comp.chain_components(force, split) and split:
                    self._unroll_component(comp)
        if self.chain:
            for coup in self.eic:
                self._chain_component(coup)
            self.eic.clear()
            for coup in self.eoc:
                self._chain_component(coup)
            self.eoc.clear()
            for coup in self.ic:
                self._chain_component(coup)
            self.ic.clear()
            if split:
                self._split()
            if self.parent.chain:
                self._unroll()
        return self.chain

    def _split(self):
        # TODO detectar hijos acoplados y cargarnoslos
        pass

    def _unroll(self):
        # TODO: el parent es una cadena, nosotros también: primero apendizar listas de los puertos de salida y entrada
        # TODO Despues hay que añadir los componentes hijo a el padre
        # TODO cargarse EICs, EOCs. Crear puertos auxiliares etc. para dividir el hijo correctamente
        pass

    @staticmethod
    def _chain_component(coup: Coupling):
        coup.port_from.couplings_to.append(coup.port_to)
        coup.port_to.couplings_from.append(coup.port_from)

    def flatten(self) -> Tuple[List, List]:
        """
        Flattens coupled model (i.e., parent coupled model inherits the connection of the model).
        :return: Components, EICs, EOCs and ICs to be transferred to parent
        """
        new_comp = list()   # list with children components to be inherited by parent
        new_coup = list()   # list with couplings to be inherited by parent

        if not self.chain:  # Chains cannot be flattened
            old_comp = list()       # list with children coupled models to be deleted
            inherit_comp = list()   # list with components to be inherited from children
            inherit_coup = list()

            for comp in self.components:
                if isinstance(comp, Coupled) and not comp.chain:  # Propagate flattening to children coupled models
                    comps, coup = comp.flatten()
                    old_comp.append(comp)
                    inherit_comp.extend(comps)
                    inherit_coup.extend(coup)

            for comp in old_comp:
                self._remove_couplings(comp)
                self.components.remove(comp)

            for comp in inherit_comp:
                self.add_component(comp)
            for coup in inherit_coup:
                self.add_coupling(coup.port_from, coup.port_to)

            if self.parent is not None:  # If module is not root, trigger the flatten process
                new_comp.extend(self.components)

                left_bridge_eic = self._create_left_bridge(self.parent.eic)
                new_coup.extend(self._complete_left_bridge(left_bridge_eic))

                left_bridge_ic = self._create_left_bridge(self.parent.ic)
                right_bridge_ic = self._create_right_bridge(self.parent.ic)
                new_coup.extend(self._complete_left_bridge(left_bridge_ic))
                new_coup.extend(self._complete_right_bridge(right_bridge_ic))

                right_bridge_eoc = self._create_right_bridge(self.parent.eoc)
                new_coup.extend(self._complete_right_bridge(right_bridge_eoc))

                new_coup.extend(self.ic)

        return new_comp, new_coup

    def _remove_couplings(self, child):
        for in_port in child.in_ports:
            self.__remove_couplings(in_port, self.eic)
            self.__remove_couplings(in_port, self.ic)
        for out_port in child.out_ports:
            self.__remove_couplings(out_port, self.ic)
            self.__remove_couplings(out_port, self.eoc)

    @staticmethod
    def __remove_couplings(port, couplings):
        to_remove = list()
        for coup in couplings:
            if coup.port_to == port or coup.port_from == port:
                to_remove.append(coup)
        for coup in to_remove:
            couplings.remove(coup)

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

    def _complete_left_bridge(self, bridge):
        couplings = list()
        for coup in self.eic:
            ports = bridge[coup.port_from]
            for port in ports:
                couplings.append(Coupling(port, coup.port_to))
        return couplings

    def _complete_right_bridge(self, bridge):
        couplings = list()
        for coup in self.eoc:
            ports = bridge[coup.port_to]
            for port in ports:
                couplings.append(Coupling(coup.port_from, port))
        return couplings
