import logging
import pkg_resources
from abc import ABC, abstractmethod
from typing import Any, Callable, Dict, List, NoReturn, Optional, Set, Tuple, Type, TypeVar
from .models import Atomic, Component, Port


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

    def add_target_component(self, component: Atomic) -> NoReturn:
        self.target_components.add(component)

    def add_target_port(self, port: Port) -> NoReturn:
        parent: Optional[Component] = port.parent
        if parent is None:
            raise ValueError('Port {} does not have a parent component', port.name)
        self.target_ports.add(port)

    # TODO methods for adding models and ports according to regular expressions, type matching, etc.

    # TODO methods for adding/removing mappings

    @abstractmethod
    def initialize_transducer(self) -> NoReturn:
        """Executes any required action before starting simulation (e.g., creating output file)."""
        pass

    @abstractmethod
    def remove_transducer(self) -> NoReturn:
        """Executes any required action before stopping simulation (e.g., removing incomplete output files)."""
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
    def bulk_state_data(self, sim_time: float, model_name: str, **kwargs) -> NoReturn:
        """
        Executes any required action for bulking the new state of a model to the destination (e.g., writing a file).
        :param sim_time: simulation time that indicates when the state changed.
        :param model_name: name of the atomic model that (potentially) changed its state.
        :param kwargs: any additional parameter that may be stored by the transducer.
        """
        pass

    @abstractmethod
    def bulk_event_data(self, sim_time: float, model_name: str, port_name: str, **kwargs) -> NoReturn:
        """
        Executes any required action for bulking new messages in a port to the destination (e.g., writing a file).
        :param sim_time: simulation time that indicates when the new messages were created.
        :param model_name: name of the model that contains the port with the event(s).
        :param port_name: name of the port that contains the event(s).
        :param kwargs: any additional parameter that may be stored by the transducer.
        """
        pass

    def activate_transducer(self, sim_time: float, components: List[Atomic], ports: List[Port]) -> NoReturn:
        """
        Activates transducer and stores any state change or new event.
        :param sim_time: current simulation time.
        :param components: list of components that have changed their state.
        :param ports: list of ports that contain new events.
        """
        try:
            self.set_up_transducer()
            for component in components:
                if component in self.target_components:
                    try:
                        fields: Dict[str, Any] = {key: value[1](component) for key, value in self.state_mapper.items()}
                    except:
                        logging.warning('Transducer {} failed when mapping '
                                        'extra state values of model {}.'.format(self.transducer_id, component.name))
                        continue
                    else:
                        self.bulk_state_data(sim_time, component.name, **fields)
            for port in ports:
                if port in self.target_ports:
                    for event in port.values:
                        try:
                            fields: Dict[str, Any] = {key: value[1](event) for key, value in self.event_mapper.items()}
                        except:
                            logging.warning('Transducer {} failed when mapping '
                                            'extra event values of event {}.'.format(self.transducer_id, str(event)))
                            continue
                        else:
                            self.bulk_event_data(sim_time, port.parent.name, port.name, **fields)
        finally:
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
