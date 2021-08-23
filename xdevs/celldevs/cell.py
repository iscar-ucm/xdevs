from abc import ABC, abstractmethod
from copy import deepcopy
from typing import Any, Dict, Generic, List, NoReturn, Optional, Tuple, Type, TypeVar
from xdevs.celldevs.inout import InPort, OutPort, DelayedOutput, DelayedOutputs
from xdevs.models import Atomic


T = TypeVar('T')  # Variable type used for generic types

C = TypeVar('C')  # Variable type used for cell IDs
S = TypeVar('S')  # Variable type used for cell states
V = TypeVar('V')  # Variable type used for cell vicinities


class CellConfig(Generic[C, S, V]):
    def __init__(self, config_id: str, c_type: Type[C], s_type: Type[S], v_type: Type[V], **kwargs):
        self.config_id: str = config_id
        self.c_type: Type[C] = c_type
        self.s_type: Type[S] = s_type
        self.v_type: Type[V] = v_type

        self.cell_type: str = kwargs['cell_type']
        self.delay_type: str = kwargs.get('delay', 'inertial')
        self.cell_config: Optional[Any] = kwargs.get('config')
        self.state: Optional[Any] = kwargs.get('state')
        self.raw_neighborhood: List[Dict] = kwargs.get('neighborhood', list())
        self.cell_map: Optional[List[C]] = None if self.default else self._load_map(*kwargs.get('cell_map', list()))
        self.eic: List[Tuple[str, str]] = self._parse_couplings(kwargs.get('eic', list()))
        self.ic: List[Tuple[str, str]] = self._parse_couplings(kwargs.get('ic', list()))  # TODO remove this?
        self.eoc: List[Tuple[str, str]] = self._parse_couplings(kwargs.get('eoc', list()))

    @property
    def default(self) -> bool:
        return self.config_id == 'default'

    def apply_patch(self, config_id: str, **kwargs):
        self.config_id = config_id
        self.cell_type = kwargs.get('cell_type', self.cell_type)
        self.delay_type = kwargs.get('delay', self.delay_type)
        if 'config' in kwargs:
            self.cell_config = self._patch_dict(self.cell_config, kwargs['config']) \
                if isinstance(self.cell_config, Dict) else kwargs['config']
        if 'state' in kwargs:
            self.state = self._patch_dict(self.state, kwargs['state']) \
                if isinstance(self.state, Dict) else kwargs['state']
        self.raw_neighborhood = kwargs.get('neighborhood', self.raw_neighborhood)
        if 'cell_map' in kwargs:
            self.cell_map = self._load_map(kwargs['cell_map'])
        if 'eic' in kwargs:
            self.eic = self._parse_couplings(kwargs['eic'])
        if 'ic' in kwargs:
            self.ic = self._parse_couplings(kwargs['ic'])
        if 'eoc' in kwargs:
            self.eoc = self._parse_couplings(kwargs['eoc'])

    def load_state(self) -> S:
        return self._load_value(self.s_type, self.state)

    def load_neighborhood(self) -> Dict[C, V]:
        neighbors: Dict[C, V] = dict()
        for neighborhood in self.raw_neighborhood:
            for neighbor, vicinity in neighborhood.items():
                neighbors[self.c_type(neighbor)] = self._load_vicinity(vicinity)
        return neighbors

    def _load_map(self, *args) -> List[C]:
        return [self.c_type(self.config_id)]

    def _load_vicinity(self, vicinity: Any):
        return self._load_value(self.v_type, vicinity)

    @classmethod
    def _patch_dict(cls, d: Dict, patch: Dict) -> Dict:
        for k, v in patch.items():
            d[k] = cls._patch_dict(d[k], v) if isinstance(v, Dict) and k in d and isinstance(d[k], Dict) else v
        return d

    @staticmethod
    def _parse_couplings(couplings: List[List[str]]) -> List[Tuple[str, str]]:
        return [(coupling[0], coupling[1]) for coupling in couplings]

    @staticmethod
    def _load_value(t_type: Type[T], params: Any) -> T:
        params = deepcopy(params)
        if isinstance(params, Dict):
            return t_type(**params)
        elif isinstance(params, List):
            return t_type(*params)
        elif params is not None:
            return t_type(params)
        return t_type()


class Cell(Atomic, ABC, Generic[C, S, V]):
    def __init__(self, cell_id: C, config: CellConfig[C, S, V]):
        super().__init__(str(cell_id))
        self._clock: float = 0
        self.cell_id: C = cell_id
        self.cell_state: S = config.load_state()
        self.neighborhood: Dict[C, V] = config.load_neighborhood()

        self.input_ports: List[InPort] = list()
        self.delayed_output_ports: DelayedOutput[S] = DelayedOutputs.create_celldevs_delay(config.delay_type)

    def add_in_port(self, in_port: InPort) -> NoReturn:
        self.input_ports.append(in_port)
        super().add_in_port(in_port.port)

    def add_out_port(self, out_port: OutPort) -> NoReturn:
        self.delayed_output_ports.add_out_port(out_port)
        super().add_out_port(out_port.port)

    @abstractmethod
    def local_computation(self, cell_state: S) -> S:
        pass

    @abstractmethod
    def output_delay(self, cell_state: S) -> float:
        pass

    def deltint(self) -> NoReturn:
        self._clock += self.sigma
        self.delayed_output_ports.clean(self._clock)
        self.sigma = self.delayed_output_ports.next_state() - self._clock

    def deltext(self, e: float) -> NoReturn:
        self._clock += e
        self.sigma -= e
        for in_port in self.input_ports:
            in_port.read_new_events()

        new_state = self.local_computation(deepcopy(self.cell_state))
        if new_state != self.cell_state:
            state = deepcopy(new_state)
            self.delayed_output_ports.add_to_buffer(self._clock + self.output_delay(state), state)
            self.sigma = self.delayed_output_ports.next_time() - self._clock
        self.cell_state = new_state

    def lambdaf(self) -> NoReturn:
        self.delayed_output_ports.send_events(self._clock + self.sigma)

    def initialize(self) -> NoReturn:
        pass

    def exit(self) -> NoReturn:
        pass
