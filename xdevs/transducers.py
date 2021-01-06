import logging
import pkg_resources
from abc import ABC, abstractmethod
from math import isinf, isnan
from typing import Any, Callable, Dict, List, NoReturn, Optional, Set, Tuple, Type, TypeVar, Union
from .models import Atomic, Component, Port


def unknown_field_to_string(x: Any) -> str:
    """Modifies field of unknown type to string"""
    return str(x)


def remove_special_numbers(x: Union[int, float]) -> Optional[Union[int, float]]:
    """Modifies integer and float types to avoid infinity and NaNs (DBs usually are not able to deal with them)"""
    if not (x is None or isnan(x) or isinf(x)):
        return x
    return None

class Transducer(ABC):

    T = TypeVar('T')
    transducer_id: str
    target_components: Set[Atomic]
    target_ports: Set[Port]
    state_mapper: Dict[str, Tuple[Type[T], Callable[[Atomic], T]]]
    event_mapper: Dict[str, Tuple[Type[T], Callable[[Any], T]]]

    def __init__(self, **kwargs):
        """
        Transducer for the xDEVS M&S tool.
        :param transducer_id: ID of the transducer.
        """
        self.transducer_id = kwargs.get('transducer_id')
        self.target_components = set()
        self.target_ports = set()
        self.state_mapper = {'phase': (str, lambda x: x.phase), 'sigma': (float, lambda x: x.sigma)}
        self.event_mapper = {'value': (str, lambda x: str(x))}
        self.state_inserts: List[Dict] = list()
        self.event_inserts: List[Dict] = list()
        self._remove_special_numbers: bool = False

    def activate_remove_special_numbers(self):
        logging.warning('Transducer {} does not support special number values (e.g., infinity). '
                        'It will automatically substitute these values by None'.format(self.transducer_id))
        self._remove_special_numbers = True

    def add_target_component(self, component: Atomic) -> NoReturn:
        self.target_components.add(component)

    def add_target_port(self, port: Port) -> NoReturn:
        parent: Optional[Component] = port.parent
        if parent is None:
            raise ValueError('Port {} does not have a parent component', port.name)
        self.target_ports.add(port)

    # TODO methods for adding models and ports according to regular expressions, type matching, etc.

    @abstractmethod
    def is_data_type_unknown(self, field_type) -> bool:
        """
        Returns whether or not the data type is known by the transducer.
        :param field_type: data type
        :return: True if data type is known.
        """
        pass

    @abstractmethod
    def initialize_transducer(self) -> NoReturn:
        """Executes any required action before starting simulation (e.g., creating output file)."""
        pass

    @abstractmethod
    def bulk_data(self, state_inserts: List[Dict[str, Any]], event_inserts: List[Dict[str, Any]]) -> NoReturn:
        """Executes any required action for bulking new data to the destination."""
        pass

    def activate_transducer(self, sim_time: float, components: List[Atomic], ports: List[Port]) -> NoReturn:
        """
        Activates transducer and stores any state change or new event.
        :param sim_time: current simulation time.
        :param components: list of components that have changed their state.
        :param ports: list of ports that contain new events.
        """
        for component in components:
            if component in self.target_components:
                extra_fields = self.map_extra_fields(component, self.state_mapper)
                self.state_inserts.append({'sim_time': sim_time, 'model_name': component.name, **extra_fields})
        for port in ports:
            if port in self.target_ports:
                for event in port.values:
                    extra_fields = self.map_extra_fields(event, self.event_mapper)
                    self.event_inserts.append({'sim_time': sim_time, 'model_name': port.parent.name,
                                               'port_name': port.name, **extra_fields})
        if self.state_inserts or self.event_inserts:
            self.bulk_data(self.state_inserts, self.event_inserts)
            self.state_inserts.clear()
            self.event_inserts.clear()

    def map_extra_fields(self, target: Any, field_mapper: dict) -> Dict[str, Any]:
        """
        Maps fields from target object according to a field mapper.
        :param target: target object that contains the additional data.
        :param field_mapper: field mapper. It has the following structure: {'field_id': (data_type, getter_function)}.
        :return: dictionary with the values to be bulked to the destination.
        """
        extra_fields: Dict[str, Any] = dict()
        for field_id, (field_type, field_mapper) in field_mapper.items():
            field_value = field_mapper(target)  # subtract extra field from target
            if self.is_data_type_unknown(field_type):
                field_value = str(field_value)  # unknown data types are automatically converted to string
            elif field_type in [int, float] and self._remove_special_numbers:
                if field_value is not None and (isnan(field_value) or isinf(field_value)):
                    field_value = None  # special numeric values are changed to None when required
            extra_fields[field_id] = field_value
        return extra_fields


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
