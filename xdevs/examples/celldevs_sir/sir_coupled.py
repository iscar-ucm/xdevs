from xdevs.transducers import Transducer, Transducers
from xdevs.celldevs.coupled import CoupledGridCellDEVS
from xdevs.celldevs.grid import GridCellConfig, GridCell
from .sir_cell import State, Vicinity, SIRGridCell


class SIRGridCoupled(CoupledGridCellDEVS[State, Vicinity]):
    def __init__(self, config_file: str):
        super().__init__(State, Vicinity, config_file)
        self.add_in_port()

        self.raw_config['transducer']
        self.transducer = Transducers.create_transducer()

    def create_cell(self, cell_type: str, cell_config: GridCellConfig[State, Vicinity]) -> GridCell[State, Vicinity]:
        return SIRGridCell(cell_config)



if __name__ == '__main__':
    config_path = 'scenario.json'
    pass
