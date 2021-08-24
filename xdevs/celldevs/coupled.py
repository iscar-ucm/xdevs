import json
from abc import abstractmethod, ABC
from copy import deepcopy
from typing import Generic, List, Optional, Tuple, Type, TypeVar
from xdevs.celldevs.cell import Cell, CellConfig
from xdevs.celldevs.grid import GridCell, GridCellConfig, GridScenario
from xdevs.models import Coupled, Dict, Port
from xdevs.transducers import Transducer, Transducers


C = TypeVar('C')  # Variable type used for cell IDs
S = TypeVar('S')  # Variable type used for cell states
V = TypeVar('V')  # Variable type used for cell vicinities


class CoupledCellDEVS(Coupled, ABC, Generic[C, S, V]):
    def __init__(self, c_type: Type[C], s_type: Type[S], v_type: Type[V], config_file: str, name: Optional[str] = None):
        super().__init__(name)
        self.c_type: Type[C] = c_type
        self.s_type: Type[S] = s_type
        self.v_type: Type[V] = v_type
        with open(config_file) as file:
            self.raw_config = json.load(file)
        self._configs: Dict[str, CellConfig[C, S, V]] = dict()
        self._cells: Dict[C, Tuple[Cell[C, S, V], CellConfig]] = dict()
        self._transducers: List[Transducer] = list()

    def load_config(self):
        raw_configs = self.raw_config['cells']
        default_config: CellConfig[C, S, V] = self._load_default_config(raw_configs['default'])
        self._configs = {'default': default_config}
        for config_id, raw_config in raw_configs.items():
            if config_id != 'default':
                config = deepcopy(default_config)
                config.apply_patch(config_id, **raw_config)
                self._configs[config_id] = config

    def load_cells(self):
        for cell_config in self._configs.values():
            if not cell_config.default:
                for cell_id in cell_config.cell_map:
                    if cell_id in self._cells:
                        raise ValueError('cell with the same ID already exists')
                    cell: Cell[C, S, V] = self.create_cell(cell_config.cell_type, cell_config)
                    self._cells[cell_id] = (cell, cell_config)
                    self.add_component(cell)

    def load_couplings(self):
        for cell_to, cell_config in self._cells.values():
            for port_from, port_to in cell_config.eic:
                self.add_coupling(self.get_in_port(port_from), cell_to.get_in_port(port_to))
            for neighbor in cell_to.neighborhood:
                cell_from = self._cells[neighbor][0]
                for port_from, port_to in cell_config.ic:
                    self.add_coupling(cell_from.get_out_port(port_from), cell_to.get_in_port(port_to))
            for port_from, port_to in cell_config.eoc:
                self.add_coupling(cell_to.get_out_port(port_from), self.get_out_port(port_to))

    def load_transducers(self):
        if self.raw_config["transducers"]:
            sink: Port[Tuple[C, S]] = Port(tuple, 'sink')
            self.add_out_port(sink)
            for cell in self.components:
                assert isinstance(cell, Cell)
                self.add_coupling(cell.out_celldevs.port, sink)
            for trans_id, trans_config in self.raw_config["transducers"].items():
                transducer: Transducer = Transducers.create_transducer(trans_id, **trans_config)
                transducer.add_target_port(sink)

                transducer.add_event_field("cell_id", self.c_type, lambda x: x[0])
                transducer.add_event_field("cell_state", self.s_type, lambda x: x[0])
                self._transducers.append(transducer)

    def _load_default_config(self, raw_config: Dict) -> CellConfig[C, S, V]:
        return CellConfig('default', self.c_type, self.s_type, self.v_type, **raw_config)

    @abstractmethod
    def create_cell(self, cell_type: str, cell_config: CellConfig[C, S, V]) -> Cell[C, S, V]:
        pass


class CoupledGridCellDEVS(CoupledCellDEVS[Tuple[int, ...], S, V], ABC, Generic[S, V]):

    _configs: Dict[str, GridCellConfig]

    def __init__(self, s_type: Type[S], v_type: Type[V], config_file: str):
        super().__init__(tuple, s_type, v_type, config_file)
        self.scenario: GridScenario = GridScenario(**self.raw_config['scenario'])

    def load_cells(self):
        super().load_cells()
        default_config = self._configs['default']
        for cell_id in self.scenario.iter_cells():
            if cell_id not in self._cells:
                cell: GridCell[S, V] = self.create_cell(default_config.cell_type, default_config)
                self._cells[cell_id] = (cell, default_config)
                self.add_component(cell)

    def _load_default_config(self, raw_config: Dict) -> GridCellConfig[S, V]:
        return GridCellConfig(self.scenario, 'default', self.s_type, self.v_type, **raw_config)

    @abstractmethod
    def create_cell(self, cell_type: str, cell_config: GridCellConfig[S, V]) -> GridCell[S, V]:
        pass
