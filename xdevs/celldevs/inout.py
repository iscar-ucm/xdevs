from abc import ABC, abstractmethod
import math
import pkg_resources
from typing import ClassVar, Dict, Generic, NoReturn, Optional, Type, TypeVar
from xdevs.models import Port
from xdevs.transducers import Transducible, T

C = TypeVar('C')  # Variable type used for cell IDs
S = TypeVar('S')  # Variable type used for cell states


class CellMessage(Transducible, Generic[C, S]):

    state_t: ClassVar[Type[S]] = None

    def __init__(self, cell_id: C, cell_state: S):
        self.cell_id = cell_id
        self.cell_state = cell_state

    @classmethod
    def transducible_fields(cls) -> Dict[str, Type[T]]:
        if issubclass(cls.state_t, Transducible):
            return {'cell_id': str, **cls.state_t.transducible_fields()}
        return {'cell_id': str, 'cell_state': str}

    def transduce(self) -> Dict[str, T]:
        if isinstance(self.cell_state, Transducible):
            return {'cell_id': str(self.cell_id), **self.cell_state.transduce()}
        return {'cell_id': str(self.cell_id), 'cell_state': str(self.cell_state)}


class InPort(Generic[C, S]):
    def __init__(self, serve: bool = False):
        """
        Cell-DEVS in port.
        :param serve: set to True if the port is going to be accessible via RPC server. Defaults to False.
        """
        self.port: Port[CellMessage[C, S]] = Port(CellMessage, 'in_celldevs', serve)
        self.history: Dict[C, S] = dict()

    def read_new_events(self):
        """It stores the latest incoming events into self.history"""
        for cell_message in self.port.values:
            self.history[cell_message.cell_id] = cell_message.cell_state

    def get(self, cell_id: C) -> Optional[S]:
        """
        Returns latest received event.
        :param cell_id: ID of the cell that sent the event.
        :return: latest received event. If no event has been received, it returns None.
        """
        return self.history.get(cell_id)


class DelayedOutput(Generic[C, S], ABC):
    def __init__(self, cell_id: C, serve: bool = False):
        """
        Cell-DEVS delayed output port. This is an abstract base class.
        :param cell_id: ID of the cell that owns this delayed output.
        :param serve: set to True if the port is going to be accessible via RPC server. Defaults to False.
        """
        self.cell_id = cell_id
        self.port: Port[CellMessage[C, S]] = Port(CellMessage, 'out_celldevs', serve)

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

    def send_events(self, time: float) -> NoReturn:
        """
        If there is an scheduled state, it sends a new event via every Cell-DEVS output port.
        :param time: current simulation time.
        """
        if self.next_time() <= time:
            self.port.add(CellMessage(self.cell_id, self.next_state()))

    def clean(self, time: float) -> NoReturn:
        """
        It cleans all the outdated scheduled cell states.
        :param time: current simulation time.
        """
        while self.next_time() < math.inf and self.next_time() <= time:
            self.pop_state()


class DelayedOutputs:

    _plugins: Dict[str, Type[DelayedOutput]] = {
        ep.name: ep.load() for ep in pkg_resources.iter_entry_points('xdevs.plugins.celldevs.delayed_outputs')
    }

    @staticmethod
    def add_plugin(delay_id: str, plugin: Type[DelayedOutput]) -> NoReturn:
        if delay_id in DelayedOutputs._plugins:
            raise ValueError('xDEVS Cell-DEVS delayed output plugin with name "{}" already exists'.format(delay_id))
        DelayedOutputs._plugins[delay_id] = plugin

    @staticmethod
    def create_delayed_output(delay_id: str, cell_id: C, serve: bool = False) -> DelayedOutput:
        if delay_id not in DelayedOutputs._plugins:
            raise ValueError('xDEVS Cell-DEVS delayed output plugin with name "{}" not found'.format(delay_id))
        return DelayedOutputs._plugins[delay_id](cell_id, serve)
