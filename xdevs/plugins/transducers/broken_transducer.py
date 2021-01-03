from typing import NoReturn
from ...transducers import Transducer
try:
    import broken  # This will fail -> we have a flag to avoid
    _dependencies_ok = True
except ModuleNotFoundError:
    _dependencies_ok = False


class BrokenTransducer(Transducer):
    """Broken transducer. It will be loaded automatically to the Transducers factory"""
    def initialize_transducer(self) -> NoReturn:
        pass

    def bulk_state_data(self, time: float, name: str, phase: str, sigma: float, **kwargs) -> NoReturn:
        pass

    def bulk_event_data(self, time: float, model_name: str, port_name: str, **kwargs) -> NoReturn:
        pass

    def set_up_transducer(self) -> NoReturn:
        pass

    def tear_down_transducer(self) -> NoReturn:
        pass

    def __init__(self, **kwargs):
        if not _dependencies_ok:  # If we try to create a broken transducer, we will raise an exception
            raise ImportError('Dependencies are not imported')
        super().__init__(**kwargs)
