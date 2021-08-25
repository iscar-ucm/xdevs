import math

from xdevs.celldevs.coupled import CoupledGridCellDEVS
from xdevs.celldevs.grid import C, GridCellConfig, GridCell
from xdevs.celldevs.inout import CellMessage
from xdevs.models import Port
from xdevs.sim import Coordinator
from xdevs.transducers import Transducer, Transducers
from sir_cell import State, Vicinity, SIRGridCell


class SIRGridCoupled(CoupledGridCellDEVS[State, Vicinity]):
    def __init__(self, config_file: str):
        super().__init__(State, Vicinity, config_file)
        self.sink_port = Port(CellMessage, 'out_sink')
        self.add_out_port(self.sink_port)

        self.load_config()
        self.load_cells()
        self.load_couplings()

    def create_cell(self, cell_type: str, cell_id: C, cell_config: GridCellConfig[State, Vicinity]) -> GridCell[State, Vicinity]:
        return SIRGridCell(cell_id, cell_config)


if __name__ == '__main__':
    model = SIRGridCoupled('scenario.json')

    transducer: Transducer = Transducers.create_transducer('csv', transducer_id='transducer', event_type=CellMessage,
                                                           include_names=False, exhaustive=True)
    transducer.add_target_port(model.sink_port)

    coordinator = Coordinator(model)
    coordinator.add_transducer(transducer)
    coordinator.initialize()
    coordinator.simulate_time(math.inf)
