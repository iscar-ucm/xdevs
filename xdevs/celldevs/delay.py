from abc import ABC, abstractmethod
import pkg_resources
from typing import Dict, Generic, NoReturn, Type, TypeVar


T = TypeVar('T')


class CellDEVSDelay(Generic[T], ABC):
    def __init__(self, **kwargs):
        pass

    @abstractmethod
    def add_to_buffer(self, when: float, event: T):
        pass

    @abstractmethod
    def next_time(self) -> float:
        pass

    @abstractmethod
    def next_event(self) -> T:
        pass

    @abstractmethod
    def pop_event(self):
        pass


class CellDEVSDelays:

    _plugins: Dict[str, Type[CellDEVSDelay]] = {ep.name: ep.load()
                                                for ep in pkg_resources.iter_entry_points('xdevs.plugins.celldevs_delays')}

    @staticmethod
    def add_plugin(name: str, plugin: Type[CellDEVSDelay]) -> NoReturn:
        if name in CellDEVSDelays._plugins:
            raise ValueError('xDEVS Cell-DEVS delay plugin with name "{}" already exists'.format(name))
        CellDEVSDelays._plugins[name] = plugin

    @staticmethod
    def create_celldevs_delay(name: str, **kwargs) -> CellDEVSDelay:
        if name not in CellDEVSDelays._plugins:
            raise ValueError('xDEVS Cell-DEVS delay plugin with name "{}" not found'.format(name))
        return CellDEVSDelays._plugins[name](**kwargs)
