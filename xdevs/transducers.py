import inspect
import itertools
import logging
import pkg_resources
import re
from abc import ABC, abstractmethod
from math import isinf, isnan
from typing import Any, Callable, Dict, List, NoReturn, Optional, Set, Tuple, Type, TypeVar, Iterable
from xdevs.models import Atomic, Component, Coupled, Port


class Transducer(ABC):

    T = TypeVar('T')
    state_mapper: Dict[str, Tuple[Type[T], Callable[[Atomic], T]]]
    event_mapper: Dict[str, Tuple[Type[T], Callable[[Any], T]]]

    def __init__(self, **kwargs):
        """
        Transducer for the xDEVS M&S tool.
        :param str transducer_id: ID of the transducer.
        :param bool exhaustive: determines if the output contains the state of the target components for each iteration
        (True) or only includes the change states (False).
        """
        self.active: bool = True
        self.transducer_id: str = kwargs.get('transducer_id', None)
        if self.transducer_id is None:
            raise AttributeError("You must specify a transducer ID.")

        self.exhaustive: bool = kwargs.get('exhaustive', False)
        self.target_components: Set[Atomic] = set()
        self.target_ports: Set[Port] = set()
        self.imminent_components: Optional[List[Atomic]] = None if self.exhaustive else []
        self.imminent_ports: Optional[List[Port]] = None if self.exhaustive else []

        self.state_mapper = {'phase': (str, lambda x: x.phase), 'sigma': (float, lambda x: x.sigma)}
        self.event_mapper = {'value': (str, lambda x: str(x))}

        self.supported_data_types: Iterable[type] = self.create_known_data_types_map()
        self._remove_special_numbers: bool = False

    def activate_remove_special_numbers(self):
        logging.warning('Transducer {} does not support special number values (e.g., infinity). '
                        'It will automatically substitute these values by None'.format(self.transducer_id))
        self._remove_special_numbers = True

    def add_target_component(self, component: Atomic or Coupled, *filters) -> NoReturn:
        components = self._iterate_components(component)
        self.target_components |= self._apply_filters(filters, components)

    def _iterate_components(self, root_comp, include_coupled=False):
        if isinstance(root_comp, Atomic):
            yield root_comp
        else:  # Coupled
            if include_coupled:
                yield root_comp
            for child_comp in root_comp.components:
                yield from self._iterate_components(child_comp, include_coupled=include_coupled)

    def add_target_port(self, port: Port) -> NoReturn:
        parent: Optional[Component] = port.parent
        if parent is None:
            raise ValueError('Port {} does not have a parent component', port.name)
        self.target_ports.add(port)

    def add_target_ports_by_component(self, component, component_filters=None, port_filters=None):
        components = self._iterate_components(component, include_coupled=True)
        filtered_components = Transducer._apply_filters(component_filters, components)
        for comp in filtered_components:
            comp_ports = itertools.chain(comp.in_ports, comp.out_ports)
            filtered_comp_ports = Transducer._apply_filters(port_filters, comp_ports)
            self.target_ports |= filtered_comp_ports

    def add_imminent_model(self, component):
        if not self.exhaustive and self.active:
            self.imminent_components.append(component)

    def add_imminent_port(self, port):
        if not self.exhaustive and self.active:
            self.imminent_ports.append(port)

    def filter_components(self, *filters):
        self.target_components = self._apply_filters(filters, self.target_components)

    @staticmethod
    def _apply_filters(filters, entities):
        """
        Filter target components or ports.
        :param filters: it can be a callable (lambda model: condition(model)) a
        regex (to filter based on the components name), or a type (to keep instances of a specific name)
        :param entities: components or ports to filter
        """
        filtered_entities = set(entities)
        if filters is not None:
            if type(filters) not in (tuple, list):
                filters = (filters,)
            for entity_filter in filters:
                if inspect.isfunction(entity_filter):
                    filtered_entities = set(filter(entity_filter, filtered_entities))
                elif type(entity_filter) is str:
                    filtered_entities = set(filter(lambda c: re.match(entity_filter, c.name), filtered_entities))
                else:
                    filtered_entities = set(filter(lambda c: isinstance(c, entity_filter), filtered_entities))

        return filtered_entities

    def pause(self):
        self.active = False

    def resume(self):
        self.active = True

    @abstractmethod
    def create_known_data_types_map(self) -> Iterable[type]:
        """
        To do.
        :return:
        """
        pass

    @abstractmethod
    def initialize(self) -> NoReturn:
        """Executes any required action before starting simulation (e.g., creating output file)."""
        pass

    @abstractmethod
    def exit(self) -> NoReturn:
        """Executes any required action after complete simulation (e.g., close output file)."""
        pass

    @abstractmethod
    def bulk_data(self, sim_time: float):
        """Executes any required action for bulking new data to the destination."""
        pass

    def trigger(self, sim_time: float):
        if self.active:
            self.bulk_data(sim_time)

            if not self.exhaustive:
                self.imminent_components.clear()
                self.imminent_ports.clear()

    def _iterate_state_inserts(self, sim_time: float):
        components = self.target_components if self.exhaustive else self.imminent_components

        # TODO: filter actually changed states
        for component in components:
            extra_fields = self.map_extra_fields(component, self.state_mapper)
            yield {'sim_time': sim_time, 'model_name': component.name, **extra_fields}

    def _iterate_event_inserts(self, sim_time: float):
        ports = self.target_ports if self.exhaustive else self.imminent_ports

        for port in ports:
            for event in port.values:
                extra_fields = self.map_extra_fields(event, self.event_mapper)
                yield {'sim_time': sim_time, 'model_name': port.parent.name, 'port_name': port.name, **extra_fields}

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
            if field_type not in self.supported_data_types:
                field_value = str(field_value)  # unknown data types are automatically converted to string
            elif self._remove_special_numbers and field_type in (int, float):
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
