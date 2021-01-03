from typing import NoReturn
from ...transducers import Transducer


class DummyTransducer(Transducer):
    """Dummy transducer. It will be loaded automatically to the Transducers factory"""

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
