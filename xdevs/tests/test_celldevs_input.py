import unittest
from xdevs.celldevs.inout import CellMessage, InPort


class CellDEVSInputTest(unittest.TestCase):
    def test_input(self):
        in_port: InPort[str, int] = InPort()
        self.assertIsNone(in_port.get('one'))

        in_port.port.add(CellMessage('one', 1))
        in_port.read_new_events()
        self.assertEqual(in_port.get('one'), 1)
        self.assertIsNone(in_port.get('two'))
        self.assertIsNone(in_port.get('three'))

        in_port.port.clear()
        in_port.read_new_events()
        self.assertEqual(in_port.get('one'), 1)
        self.assertIsNone(in_port.get('two'))
        self.assertIsNone(in_port.get('three'))

        in_port.port.add(CellMessage('two', 2))
        in_port.read_new_events()
        self.assertEqual(in_port.get('one'), 1)
        self.assertEqual(in_port.get('two'), 2)
        self.assertIsNone(in_port.get('three'))

        in_port.port.clear()
        in_port.port.add(CellMessage('two', 2))
        in_port.port.add(CellMessage('three', 3))
        in_port.read_new_events()
        self.assertEqual(in_port.get('one'), 1)
        self.assertEqual(in_port.get('two'), 2)
        self.assertEqual(in_port.get('three'), 3)


if __name__ == '__main__':
    unittest.main()
