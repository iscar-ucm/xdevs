import math
from xdevs.celldevs.inout import CellMessage
from xdevs.models import Coupled
from xdevs.sim import Coordinator
from xdevs.transducers import Transducer, Transducers
from sir_coupled import SIRGridCoupled
from sir_sink import SIRSink, State


class SIRModel(Coupled):
    def __init__(self, config_path: str):
        super().__init__()
        self.celldevs = SIRGridCoupled(config_path)
        self.sink = SIRSink('sink')

        self.add_component(self.celldevs)
        self.add_component(self.sink)
        self.add_coupling(self.celldevs.sink_port, self.sink.in_sink)


if __name__ == '__main__':
    model = SIRModel('scenario.json')

    celldevs_transducer: Transducer = Transducers.create_transducer('csv', transducer_id='celldevs',
                                                                    event_type=CellMessage, include_names=False,
                                                                    exhaustive=True)
    celldevs_transducer.add_target_port(model.celldevs.sink_port)

    sink_transducer: Transducer = Transducers.create_transducer('csv', transducer_id='sink',
                                                                event_type=State, include_names=False)
    sink_transducer.add_target_port(model.sink.out_sink)

    coordinator = Coordinator(model)
    coordinator.add_transducer(celldevs_transducer)
    coordinator.add_transducer(sink_transducer)
    coordinator.initialize()
    coordinator.simulate_time(math.inf)
