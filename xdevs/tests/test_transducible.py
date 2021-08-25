from __future__ import annotations
import unittest
from typing import Dict, Type
from xdevs.transducers import Transducer, Transducers, Transducible, T


class NonTransducibleClass:
    def __init__(self, start: int, stop: float, log: bool):
        self.start: int = start
        self.stop: float = stop
        self.log: bool = log


class TransducibleClass(NonTransducibleClass, Transducible):
    @classmethod
    def transducible_fields(cls) -> Dict[str, Type[T]]:
        return {'start': int, 'stop': float, 'log': bool}

    def transduce(self) -> Dict[str, T]:
        return {'start': self.start, 'stop': self.stop, 'log': self.log}


class MyTestCase(unittest.TestCase):
    def test_no_type(self):
        transducer: Transducer = Transducers.create_transducer('csv', transducer_id='transducer')
        event_mapper = transducer.event_mapper
        self.assertEqual(len(event_mapper), 1)
        self.assertTrue('value' in event_mapper)

    def test_non_transducible(self):
        transducer: Transducer = Transducers.create_transducer('csv', transducer_id='transducer',
                                                               event_type=NonTransducibleClass)
        event_mapper = transducer.event_mapper
        self.assertEqual(len(event_mapper), 1)
        self.assertTrue('value' in event_mapper)

    def test_transducible(self):
        transducer: Transducer = Transducers.create_transducer('csv', transducer_id='transducer',
                                                               event_type=TransducibleClass)
        event_mapper = transducer.event_mapper
        self.assertEqual(len(event_mapper), 3)
        self.assertTrue('start' in event_mapper)
        self.assertTrue('stop' in event_mapper)
        self.assertTrue('log' in event_mapper)


if __name__ == '__main__':
    unittest.main()
