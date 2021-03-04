from __future__ import annotations

import inspect
import pickle
from abc import ABC, abstractmethod
from collections import deque, defaultdict
from typing import Deque, Dict, Generator, Generic, Iterator, NoReturn, Optional, List, Tuple, Type, TypeVar

from xdevs import PHASE_ACTIVE, PHASE_PASSIVE, INFINITY


T = TypeVar('T')


class Port(Generic[T]):

    IN = "in"
    OUT = "out"

    def __init__(self, p_type: Type[T] = None, name: Optional[str] = None, serve: bool = False):
        """
        xDEVS implementation of DEVS Port.
        :param p_type: data type of events to be sent/received via the new port instance.
        :param name: name of the new port instance. Defaults to the name of the port's class.
        :param serve: set to True if the port is going to be accessible via RPC server. Defaults to False.
        """
        self.name: str = name if name else self.__class__.__name__
        self.p_type: Type[T] = p_type
        self.serve: bool = serve
        self.parent: Optional[Component] = None  # xDEVS Component that owns the port
        self.direction: Optional[str] = None     # Message flow direction of the port. It is either PORT_IN or PORT_OUT
        self._values: Deque[T] = deque()         # Bag containing messages directly written to the port
        self.links_to: List[Port] = list()       # Ports that receive data from the port (only used by output ports)
        self.links_from: List[Port] = list()     # Ports that inject data to the port (only used by input ports)

    def __bool__(self) -> bool:
        return not self.empty()

    def __len__(self) -> int:
        return sum(1 for _ in self.values)

    def __str__(self) -> str:
        return "%s.%s(%s)" % (self.parent.name, self.name, self.p_type or "None")

    def __repr__(self) -> str:
        return str(self)

    def empty(self) -> bool:
        for _ in self.values:
            return False
        return True

    def clear(self) -> NoReturn:
        self._values.clear()

    @property
    def values(self) -> Generator[T, None, None]:
        """:return: Generator function that can iterate over all the values contained in the port."""
        for val in self._values:
            yield val

        for port in self.links_from:
            for val in port._values:
                yield val

    def get(self) -> T:
        """
        :return: first value in the port.
        :raises StopIteration: if port is empty.
        """
        return next(self.values)

    def add(self, val: T) -> NoReturn:
        """
        Adds a new value to the local value bag of the port.
        :param val: event to be added.
        :raises TypeError: If event is not instance of port type.
        """
        if self.p_type is not None and not isinstance(val, self.p_type):
            raise TypeError("Value type is %s (%s expected)" % (type(val).__name__, self.p_type.__name__))
        self._values.append(val)

    def extend(self, vals: Iterator[T]) -> NoReturn:
        """
        Adds a set of new values to the local value bag of the port.
        :param vals: list containing all the values to be added.
        :raises TypeError: If one of the values is not instance of port type.
        """
        for val in vals:
            self.add(val)


class Component(ABC):
    def __init__(self, name: Optional[str] = None):
        """
        Abstract Base Class for an xDEVS model.
        :param name: name of the xDEVS model. Defaults to the name of the component's class.
        """
        self.name: str = name if name else self.__class__.__name__
        self.parent: Optional[Coupled] = None  # Parent component of this component
        self.in_ports: List[Port] = list()  # List containing all the component's input ports
        self.out_ports: List[Port] = list()  # List containing all the component's output ports

    def __str__(self) -> str:
        in_str = " ".join([p.name for p in self.in_ports])
        out_str = " ".join([p.name for p in self.out_ports])
        return "%s: InPorts[%s] OutPorts[%s]" % (self.name, in_str, out_str)

    def __repr__(self):
        return self.name

    @abstractmethod
    def initialize(self) -> NoReturn:
        pass

    @abstractmethod
    def exit(self) -> NoReturn:
        pass

    def in_empty(self) -> bool:
        """:return: True if model has not any message in all its input ports."""
        for port in self.in_ports:
            if port:
                return False
        return True

    def out_empty(self) -> bool:
        """:return: True if model has not any message in all its output ports."""
        for port in self.out_ports:
            if port:
                return False
        return True

    def add_in_port(self, port: Port) -> NoReturn:
        """
        Adds an input port to the xDEVS model.
        :param port: port to be added to the model.
        """
        port.parent = self
        port.direction = Port.IN
        self.in_ports.append(port)

    def add_out_port(self, port: Port) -> NoReturn:
        """
        Adds an output port to the xDEVS model
        :param port: port to be added to the model.
        """
        port.parent = self
        port.direction = Port.OUT
        self.out_ports.append(port)

    def get_in_port(self, name) -> Optional[Port]:
        for port in self.in_ports:
            if port.name == name:
                return port
        return None

    def get_out_port(self, name) -> Optional[Port]:
        for port in self.out_ports:
            if port.name == name:
                return port
        return None


class Coupling(Generic[T]):
    def __init__(self, port_from: Port[T], port_to: Port[T], host=None):
        """
        xDEVS implementation of DEVS couplings.
        :param port_from: DEVS transmitter port.
        :param port_to: DEVS receiver port.
        :param host: TODO documentation for this
        :raises ValueError: if coupling direction is incompatible with the target ports.
        """
        # Check that couplings are valid
        comp_from: Component = port_from.parent
        comp_to: Component = port_to.parent
        if isinstance(comp_from, Atomic) and port_from.direction == Port.IN:
            raise ValueError("Input ports whose parent is an Atomic model can not be coupled to any other port")
        if isinstance(comp_to, Atomic) and port_to.direction == Port.OUT:
            raise ValueError("Output ports whose parent is an Atomic model can not be recipient of any other port")
        if port_from.p_type is not None and port_to in inspect.getmro(port_from.p_type):
            raise ValueError("Ports don't share the same port type")
        self.port_from: Port = port_from
        self.port_to: Port = port_to
        self.host = host  # TODO identify host's variable type

    def __str__(self) -> str:
        return "(%s -> %s)" % (self.port_from, self.port_to)

    def __repr__(self) -> str:
        return str(self)

    def propagate(self) -> NoReturn:
        """Copies messages from the transmitter port to the receiver port"""
        if self.host:
            if self.port_from:
                values = list(map(lambda x: pickle.dumps(x, protocol=0).decode(), self.port_from.values))
                self.host.inject(self.port_to, values)
        else:
            self.port_to.extend(self.port_from.values)


class Atomic(Component, ABC):
    def __init__(self, name: str = None):
        """
        xDEVS implementation of DEVS Atomic Model.
        :param name: name of the atomic model. If no name is provided, it will take the class's name by default.
        """
        super().__init__(name)

        self.phase: str = PHASE_PASSIVE
        self.sigma: float = INFINITY

    @property
    def ta(self) -> float:
        """:return: remaining time for the atomic model's internal transition."""
        return self.sigma

    def __str__(self) -> str:
        return "%s(%s, %s)" % (self.name, str(self.phase), self.sigma)

    @abstractmethod
    def deltint(self) -> NoReturn:
        """Describes the internal transitions of the atomic model."""
        pass

    @abstractmethod
    def deltext(self, e: float) -> NoReturn:
        """
        Describes the external transitions of the atomic model.
        :param e: elapsed time between last transition and the external transition.
        """
        pass

    @abstractmethod
    def lambdaf(self) -> NoReturn:
        """Describes the output function of the atomic model."""
        pass

    def deltcon(self, e: float) -> NoReturn:
        """
        Describes the confluent transitions of the atomic model. By default, the internal transition is triggered first.
        :param e: elapsed time between last transition and the confluent transition.
        """
        self.deltint()
        self.deltext(0)

    def hold_in(self, phase: str, sigma: float) -> NoReturn:
        """
        Change atomic model's phase and next timeout.
        :param phase: atomic model's new phase.
        :param sigma: time remaining to the next timeout.
        """
        self.phase = phase
        self.sigma = sigma

    def activate(self, phase=PHASE_ACTIVE) -> NoReturn:
        """
        Sets next timeout to 0.
        :param phase: New phase. Defaults to "PHASE_ACTIVE".
        """
        self.phase = phase
        self.sigma = 0

    def passivate(self, phase=PHASE_PASSIVE) -> NoReturn:
        """
        Sets next timeout to 0 infinity.
        :param phase: New phase. Defaults to "PHASE_PASSIVE".
        """
        self.phase = phase
        self.sigma = INFINITY

    def continuef(self, e: float) -> NoReturn:
        """
        Reduces the next timeout by e time units.
        :param e: elapsed time to be subtracted from sigma.
        """
        self.sigma -= e


class Coupled(Component, ABC):
    def __init__(self, name: str = None):
        """
        xDEVS implementation of DEVS Coupled Model.
        :param name: name of the coupled model. If no name is provided, it will take the class's name by default.
        """
        super().__init__(name)
        self.chain: bool = False

        self.components: List[Component] = list()
        self.ic: Dict[Port, List[Coupling]] = dict()
        self.eic: Dict[Port, List[Coupling]] = dict()
        self.eoc: Dict[Port, List[Coupling]] = dict()

    def initialize(self) -> NoReturn:
        pass

    def exit(self) -> NoReturn:
        pass

    def add_coupling(self, p_from: Port, p_to: Port, host=None) -> NoReturn:
        """
        Adds coupling between two submodules of the coupled model.
        :param p_from: DEVS transmitter port.
        :param p_to: DEVS receiver port.
        :param host: TODO documentation
        :raises ValueError: if coupling is not well defined.
        """
        coupling = Coupling(p_from, p_to, host)
        if p_from.parent == self and p_to.parent in self.components:
            Coupled._safe_coup_add(self.eic, coupling)
        elif p_from.parent in self.components and p_to.parent == self:
            Coupled._safe_coup_add(self.eoc, coupling)
        elif p_from.parent in self.components and p_to.parent in self.components:
            Coupled._safe_coup_add(self.ic, coupling)
        else:
            raise ValueError("Components that compose the coupling are not submodules of coupled model")

    @staticmethod
    def _safe_coup_add(dct: Dict[Port, List[Coupling]], coup: Coupling) -> NoReturn:
        if coup.port_from not in dct:
            dct[coup.port_from] = [coup]
        else:
            dct[coup.port_from].append(coup)

    def remove_coupling(self, coupling: Coupling) -> NoReturn:
        """
        Removes coupling between two submodules of the coupled model.
        :param coupling: Couplings to be removed.
        :raises ValueError: if coupling is not found.
        """
        for key, coups in self.eic.items():
            for coup in coups:
                if coupling == coup:
                    del self.eic[key]
                    return

        for key, coups in self.eoc.items():
            for coup in coups:
                if coupling == coup:
                    del self.eoc[key]
                    return

        for key, coups in self.ic.items():
            for coup in coups:
                if coupling == coup:
                    del self.ic[key]
                    return

        raise ValueError("Coupling was not found in model definition")

    def add_component(self, component: Component) -> NoReturn:
        """
        Adds component to coupled model.
        :param component: component to be added to the Coupled model.
        """
        component.parent = self
        self.components.append(component)

    def chain_components(self, unroll: bool = True) -> NoReturn:
        """
        Chains submodules to enhance sequential execution performance.
        """
        self.chain = True
        if unroll:
            self.flatten()
        else:
            for comp in self.components:
                if isinstance(comp, Coupled):
                    comp.chain_components(unroll)

        for coup_list in self.eic.values():
            for coup in coup_list:
                self._chain_from_coupling(coup)
        # self.eic.clear()
        for coup_list in self.eoc.values():
            for coup in coup_list:
                self._chain_from_coupling(coup)
        # self.eoc.clear()
        for coup_list in self.ic.values():
            for coup in coup_list:
                self._chain_from_coupling(coup)
        # self.ic.clear()

    @staticmethod
    def _chain_from_coupling(coup: Coupling) -> NoReturn:
        coup.port_from.links_to.append(coup.port_to)
        coup.port_to.links_from.append(coup.port_from)

    def flatten(self) -> Tuple[List[Component], List[Coupling]]:
        """
        Flattens coupled model (i.e., parent coupled model inherits the connection of the model).
        :return: Components and couplings to be transferred to parent
        """
        new_comps_up = list()  # list with children components to be inherited by parent
        new_coups_up = list()  # list with couplings to be inherited by parent

        old_comps = list()  # list with children coupled models to be deleted

        for comp in self.components:
            if isinstance(comp, Coupled):  # Propagate flattening to children coupled models
                new_comps_down, new_coups_down = comp.flatten()
                old_comps.append(comp)
                for new_comp in new_comps_down:
                    self.add_component(new_comp)
                for coup in new_coups_down:
                    self.add_coupling(coup.port_from, coup.port_to)

        for comp in old_comps:
            self._remove_couplings_of_child(comp)
            self.components.remove(comp)

        if self.parent is not None:  # If module is not root, trigger the flatten process
            new_comps_up.extend(self.components)

            left_bridge_eic = self._create_left_bridge(self.parent.eic)
            new_coups_up.extend(self._complete_left_bridge(left_bridge_eic))

            left_bridge_ic = self._create_left_bridge(self.parent.ic)
            right_bridge_ic = self._create_right_bridge(self.parent.ic)
            new_coups_up.extend(self._complete_left_bridge(left_bridge_ic))
            new_coups_up.extend(self._complete_right_bridge(right_bridge_ic))

            right_bridge_eoc = self._create_right_bridge(self.parent.eoc)
            new_coups_up.extend(self._complete_right_bridge(right_bridge_eoc))

            new_coups_up.extend([c for cl in self.ic.values() for c in cl])

        return new_comps_up, new_coups_up

    def _remove_couplings_of_child(self, child: Component) -> NoReturn:
        for in_port in child.in_ports:
            self._remove_couplings(in_port, self.eic)
            self._remove_couplings(in_port, self.ic)
        for out_port in child.out_ports:
            self._remove_couplings(out_port, self.ic)
            self._remove_couplings(out_port, self.eoc)

    @staticmethod
    def _remove_couplings(port: Port, couplings: Dict[Port, List[Coupling]]) -> NoReturn:
        # Remove port from couplings list
        couplings.pop(port, None)
        # For remaining ports, remove couplings which source is the port to be removed
        to_remove = list()
        for _, coup_list in couplings.items():
            for coup in coup_list:
                if coup.port_to == port:
                    to_remove.append(coup)
        for coup in to_remove:
            couplings[coup.port_from].remove(coup)

    def _create_left_bridge(self, pc) -> Dict[Port, List[Port]]:
        bridge = defaultdict(list)
        for in_port in self.in_ports:
            for coup_list in pc.values():
                for coup in coup_list:
                    if coup.port_to == in_port:
                        bridge[in_port].append(coup.port_from)
        return bridge

    def _create_right_bridge(self, pc) -> Dict[Port, List[Port]]:
        bridge = defaultdict(list)
        for out_port in self.out_ports:
            for coup_list in pc.values():
                for coup in coup_list:
                    if coup.port_from == out_port:
                        bridge[out_port].append(coup.port_to)
        return bridge

    def _complete_left_bridge(self, bridge) -> List[Coupling]:
        couplings = list()
        for coup_list in self.eic.values():
            for coup in coup_list:
                ports = bridge[coup.port_from]
                for port in ports:
                    couplings.append(Coupling(port, coup.port_to))
        return couplings

    def _complete_right_bridge(self, bridge) -> List[Coupling]:
        couplings = list()
        for coup_list in self.eoc.values():
            for coup in coup_list:
                ports = bridge[coup.port_to]
                for port in ports:
                    couplings.append(Coupling(coup.port_from, port))
        return couplings
