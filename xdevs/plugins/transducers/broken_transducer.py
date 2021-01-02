from typing import NoReturn

from ...models import Port, Atomic
from ...sim import SimulationClock
from ...transducers import Transducer
try:
    import broken  # This will fail -> we have a flag to avoid
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class BrokenTransducer(Transducer):
    '''Broken transducer. It will be loaded automatically to the Transducers factory'''

    def set_up_transducer(self) -> NoReturn:
        pass

    def tear_down_transducer(self) -> NoReturn:
        pass

    def bulk_model_data(self, clock: SimulationClock, model: Atomic) -> NoReturn:
        pass

    def bulk_port_data(self, clock: SimulationClock, port: Port) -> NoReturn:
        pass

    def __init__(self, **kwargs):
        if not _dependencies_ok:  # If we try to create a broken transducer, we will raise an exception
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)
