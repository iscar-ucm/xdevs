from xdevs.transducers.transducers import Transducer, Transducers


class AnotherDummyTransducer(Transducer):
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
