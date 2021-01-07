from typing import NoReturn
from xdevs.transducers import Transducer, Transducers


class AnotherDummyTransducer(Transducer):
    def initialize(self) -> NoReturn:
        pass

    def bulk_state_data(self, time: float, name: str, phase: str, sigma: float, **kwargs) -> NoReturn:
        pass

    def bulk_event_data(self, time: float, model_name: str, port_name: str, **kwargs) -> NoReturn:
        pass

    def set_up_transducer(self) -> NoReturn:
        pass

    def tear_down_transducer(self) -> NoReturn:
        pass


if __name__ == '__main__':
    Transducers.add_plugin('another_dummy', AnotherDummyTransducer)
    a = Transducers.create_transducer('dummy')
    try:
        b = Transducers.create_transducer('broken')
    except ImportError:
        print('Broken transducer failed')
    c = Transducers.create_transducer('another_dummy')
    print('done')
