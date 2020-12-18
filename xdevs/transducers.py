import pkg_resources
from abc import ABC, abstractmethod
from typing import Dict, List, NoReturn, Type
from .sim import SimulationClock
from .models import Atomic, Port


class Transducer(ABC):

    transducer_id: str
    targets: List[Atomic]  # TODO more generic approach for ports (maybe we need two abstract classes that inherit this)

    def __init__(self, **kwargs):
        self.transducer_id = kwargs.get('transducer_id')
        self.targets = kwargs.get('models', list())
        # TODO callables for subtracting data in a (field_id, field_value) fashion

    @abstractmethod
    def bulk_data(self, clock: SimulationClock):
        pass


class Transducers:
    _plugins: Dict[str, Type[Transducer]] = {ep.name: ep.load()
                                             for ep in pkg_resources.iter_entry_points('xdevs.plugins.transducers')}

    @staticmethod
    def add_plugin(name: str, plugin: Type[Transducer]) -> NoReturn:
        if name in Transducers._plugins:
            raise ValueError('xDEVS transducer plugin with name "{}" already exists'.format(name))
        Transducers._plugins[name] = plugin

    @staticmethod
    def create_transducer(name: str, **kwargs) -> Transducer:
        if name not in Transducers._plugins:
            raise ValueError('xDEVS transducer plugin with name "{}" not found'.format(name))
        return Transducers._plugins[name](**kwargs)
