from abc import ABC, abstractmethod
import pkg_resources
from typing import Callable, Dict, Generic, List, NoReturn, Optional, Type, TypeVar, Union
from xdevs.models import Port

C = TypeVar('C')  # Variable type used for cell IDs
S = TypeVar('S')  # Variable type used for cell states
E = TypeVar('E')  # Variable type used for port events


class InPort(Generic[C, E]):
    def __init__(self, p_type: Type[E], port_name: Optional[str] = None,
                 serve: bool = False, classifier: Optional[Callable[[E], C]] = None):
        """
        Cell-DEVS in port.
        :param p_type: data type of events received by port.
        :param port_name: name of the port. By default, it is set to "InPort".
        :param serve: set to True if the port is going to be accessible via RPC server. Defaults to False.
        :param classifier: function to classify incoming events into different groups.
                           By default, it is set to None (i.e., messages are not classified).
        """
        self.port: Port[E] = Port(p_type, port_name, serve)
        self.classifier: Optional[Callable[[E], C]] = classifier
        self.history: Union[Optional[E], Dict[C, E]] = None if self.classifier is None else dict()

    def read_new_events(self) -> NoReturn:
        """
        It stores the latest incoming event into self.history[group], where group = self.classifier(event).
        If self.history is None, the latest event is stored into self.history (with no group classification).
        """
        if self.port:
            if self.classifier is None:
                self.history = self.port.get()
            else:
                for msg in self.port.values:
                    self.history[self.classifier(msg)] = msg

    def get(self, key: Optional[C] = None) -> Optional[S]:
        return self.history.get(key) if self.classifier is not None else self.history


class OutPort(Generic[S, E]):
    def __init__(self, port_type: Type[E], port_name: Optional[str] = None,
                 serve: bool = False, mapper: Optional[Callable[[S], E]] = None):
        """
        Cell-DEVS out port.
        :param port_type: data type of events sent by port.
        :param port_name: name of the port. By default, it is set to "OutPort".
        :param serve: set to True if the port is going to be accessible via RPC server. Defaults to False.
        :param mapper: function to map a cell state S to a port event V. By default, mapper(S) = S.
        """
        self.port: Port[E] = Port(port_type, port_name, serve)
        self.mapper = lambda x: x if mapper is None else mapper

    def add(self, cell_state: S) -> NoReturn:
        """
        Maps a cell state to an event and sends it through the port.
        :param cell_state: cell state.
        """
        self.port.add(self.mapper(cell_state))


class DelayedOutput(Generic[S], ABC):
    def __init__(self):
        """Cell-DEVS delayed output ports. This is an abstract base class."""
        self.out_ports: List[OutPort[S, E]] = list()

    @abstractmethod
    def add_to_buffer(self, when: float, state: S) -> NoReturn:
        """
        Schedules a cell state to send events.
        :param when: time at which the events must be sent.
        :param state: cell state. Events will be obtained by mapping this state.
        """
        pass

    @abstractmethod
    def next_time(self) -> float:
        """:return: next time at which events must be sent."""
        pass

    @abstractmethod
    def next_state(self) -> S:
        """:return: next cell state used to generate events."""
        pass

    @abstractmethod
    def pop_state(self) -> NoReturn:
        """removes schedule state from the delayed output."""
        pass

    def add_out_port(self, out_port: OutPort[S, E]) -> NoReturn:
        """Adds a Cell-DEVS output port to the delayed output."""
        self.out_ports.append(out_port)

    def send_events(self, time: float) -> NoReturn:
        """
        If there is an scheduled state, it sends a new event via every Cell-DEVS output port.
        :param time: current simulation time.
        """
        if self.next_time() <= time:
            next_state = self.next_state()
            for out_port in self.out_ports:
                out_port.add(next_state)

    def clean(self, time: float) -> NoReturn:
        """
        It cleans all the outdated scheduled cell states.
        :param time: current simulation time.
        """
        while self.next_time() <= time:
            self.pop_state()


class DelayedOutputs:

    _plugins: Dict[str, Type[DelayedOutput]] = {ep.name: ep.load()
                                                for ep in pkg_resources.iter_entry_points('xdevs.plugins.celldevs.delayed_outputs')}

    @staticmethod
    def add_plugin(name: str, plugin: Type[DelayedOutput]) -> NoReturn:
        if name in DelayedOutputs._plugins:
            raise ValueError('xDEVS Cell-DEVS delayed output plugin with name "{}" already exists'.format(name))
        DelayedOutputs._plugins[name] = plugin

    @staticmethod
    def create_celldevs_delay(name: str) -> DelayedOutput:
        if name not in DelayedOutputs._plugins:
            raise ValueError('xDEVS Cell-DEVS delayed output plugin with name "{}" not found'.format(name))
        return DelayedOutputs._plugins[name]()
