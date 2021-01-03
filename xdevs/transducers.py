import logging
import pkg_resources
from abc import ABC, abstractmethod
from typing import Any, Callable, Dict, List, NoReturn, Optional, Set, Tuple, Type, TypeVar
from .models import Atomic, Component, Port


class Transducer(ABC):

    T = TypeVar('T')
    transducer_id: str
    state_mapper: Dict[str, Tuple[Type[T], Callable[[Atomic], T]]]
    message_mapper: Dict[str, Tuple[Type[T], Callable[[Any], T]]]

    _target_components: Set[Atomic]
    _target_ports: Dict[Component, Set[Port]]

    def __init__(self, **kwargs):
        self.transducer_id = kwargs.get('transducer_id')
        self._target_components = kwargs.get('target_models', set())
        self._target_ports = kwargs.get('target_ports', dict())
        self.state_mapper = kwargs.get('state_mapper', dict())
        self.message_mapper = kwargs.get('message_mapper', {'value': (str, lambda x: str(x))})

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
    def initialize_transducer(self) -> NoReturn:
        """Executes any required action before starting simulation (e.g., creating output file)."""
        pass

    @abstractmethod
    def set_up_transducer(self) -> NoReturn:
        """Executes any required action before bulking new data to the destination (e.g., opening files for writing)."""
        pass

    @abstractmethod
    def tear_down_transducer(self) -> NoReturn:
        """Executes any required action after bulking new data to the destination (e.g., closing a written files)."""
        pass

    @abstractmethod
    def bulk_state_data(self, time: float, name: str, phase: str, sigma: float, **kwargs) -> NoReturn:
        """
        Executes any required action for bulking the new state of a model to the destination (e.g., writing a file).
        :param time: simulation time that indicates when the state changed.
        :param name: name of the atomic model that (potentially) changed its state.
        :param phase: new phase of the atomic model.
        :param sigma: new timeout of the new phase of the atomic model.
        :param kwargs: any additional parameter that may be stored by the transducer.
        """
        pass

    @abstractmethod
    def bulk_event_data(self, time: float, model_name: str, port_name: str, **kwargs) -> NoReturn:
        """
        Executes any required action for bulking new messages in a port to the destination (e.g., writing a file).
        :param time: simulation time that indicates when the new messages were created.
        :param model_name: name of the model that contains the port with the event(s).
        :param port_name: name of the port that contains the event(s).
        :param kwargs: any additional parameter that may be stored by the transducer.
        """
        pass

    def activate_transducer(self, time: float, imminent_components: List[Component]):
        self.set_up_transducer()
        for component in imminent_components:
            if component in self._target_components and isinstance(component, Atomic):
                try:
                    fields: Dict[str, Any] = {key: value[1](component) for key, value in self.state_mapper.items()}
                except:
                    logging.warning('Transducer {} failed when mapping '
                                    'extra state values of model {}.'.format(self.transducer_id, component.name))
                    continue
                else:
                    self.bulk_state_data(time, component.name, component.phase, component.sigma, **fields)
            for port in self._target_ports.get(component, set()):
                for event in port.values:
                    try:
                        fields: Dict[str, Any] = {key: value[1](event) for key, value in self.message_mapper.items()}
                    except:
                        logging.warning('Transducer {} failed when mapping '
                                        'extra event values of event {}.'.format(self.transducer_id, str(event)))
                        continue
                    else:
                        self.bulk_event_data(time, component.name, port.name, **fields)
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
