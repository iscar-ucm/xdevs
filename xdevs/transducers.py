import pkg_resources
from abc import ABC, abstractmethod
from typing import Dict, List, NoReturn, Optional, Type, Set
from .sim import SimulationClock
from .models import Atomic, Component, Port


class Transducer(ABC):

    transducer_id: str
    _target_components: Set[Atomic]
    _target_ports: Dict[Component, Set[Port]]

    def __init__(self, **kwargs):
        self.transducer_id = kwargs.get('transducer_id')
        self._target_components = kwargs.get('target_models', set())
        self._target_ports = kwargs.get('target_ports', dict())
        # TODO callables for subtracting data in a (field_id, field_value) fashion

    def add_target_component(self, component: Atomic) -> NoReturn:
        self._target_components.add(component)

    def add_target_port(self, port: Port) -> NoReturn:
        parent: Optional[Component] = port.parent
        if parent is None:
            raise ValueError('Port {} does not have a parent component', port.name)
        if parent not in self._target_ports:
            self._target_ports[parent] = set()
        self._target_ports[parent].add(port)

    # TODO methods for adding models and ports according to regular expressions, type matching, etc.

    @abstractmethod
    def set_up_transducer(self) -> NoReturn:
        """Executes any required action before bulking new data to the destination (e.g., opening files for writing)."""
        pass

    @abstractmethod
    def tear_down_transducer(self) -> NoReturn:
        """Executes any required action after bulking new data to the destination (e.g., closing a written files)."""
        pass

    @abstractmethod
    def bulk_model_data(self, clock: SimulationClock, model: Atomic) -> NoReturn:
        """
        Executes any required action for bulking the new state of a model to the destination (e.g., writing a file).
        :param clock: simulation clock that indicates when the state changed.
        :param model: atomic model that (potentially) changed its state.
        """
        pass

    @abstractmethod
    def bulk_port_data(self, clock: SimulationClock, port: Port) -> NoReturn:
        """
        Executes any required action for bulking new messages in a port to the destination (e.g., writing a file).
        :param clock: simulation clock that indicates when the new messages were created.
        :param port: port that contains these new messages.
        """
        pass

    def activate_transducer(self, clock: SimulationClock, imminent_components: List[Component]):
        self.set_up_transducer()
        for component in imminent_components:
            if component in self._target_components and isinstance(component, Atomic):
                # TODO preprocess new state to a generic row-like format. Then, bulk the preprocessed data
                self.bulk_model_data(clock, component)
            for port in self._target_ports.get(component, set()):
                if port:
                    # TODO preprocess new state to a generic row-like format. Then, bulk the preprocessed data
                    self.bulk_port_data(clock, port)
        self.tear_down_transducer()


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
