from typing import NoReturn

from ...models import Port, Atomic
from ...sim import SimulationClock
from ...transducers import Transducer


class DummyTransducer(Transducer):
    '''Dummy transducer. It will be loaded automatically to the Transducers factory'''

    def set_up_transducer(self) -> NoReturn:
        pass

    def tear_down_transducer(self) -> NoReturn:
        pass

    def bulk_model_data(self, clock: SimulationClock, model: Atomic) -> NoReturn:
        pass

    def bulk_port_data(self, clock: SimulationClock, port: Port) -> NoReturn:
        pass
