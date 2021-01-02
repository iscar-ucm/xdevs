from typing import NoReturn

from xdevs.models import Port, Atomic
from xdevs.sim import SimulationClock
from xdevs.transducers import Transducer, Transducers


class AnotherDummyTransducer(Transducer):
    def set_up_transducer(self) -> NoReturn:
        pass

    def tear_down_transducer(self) -> NoReturn:
        pass

    def bulk_model_data(self, clock: SimulationClock, model: Atomic) -> NoReturn:
        pass

    def bulk_port_data(self, clock: SimulationClock, port: Port) -> NoReturn:
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
