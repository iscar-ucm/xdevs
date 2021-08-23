import unittest
from xdevs.celldevs.inout import InPort


class MyTestCase(unittest.TestCase):
    def test_basic_input(self):
        in_port: InPort[None, int] = InPort(int, 'port')
        self.assertIsNone(in_port.get())

        in_port.port.add(1)
        in_port.read_new_events()
        self.assertEqual(in_port.get(), 1)

        in_port.port.clear()
        in_port.read_new_events()
        self.assertEqual(in_port.get(), 1)

        in_port.port.add(2)
        in_port.port.add(3)
        in_port.read_new_events()
        self.assertEqual(in_port.get(), 2)

    def test_mapped_input(self):
        in_port: InPort[str, int] = InPort(int, 'port', classifier=lambda x: str(x))
        self.assertIsNone(in_port.get())

        in_port.port.add(1)
        in_port.read_new_events()
        self.assertIsNone(in_port.get())
        self.assertEqual(in_port.get('1'), 1)
        self.assertIsNone(in_port.get('2'))
        self.assertIsNone(in_port.get('3'))

        in_port.port.clear()
        in_port.read_new_events()
        self.assertIsNone(in_port.get())
        self.assertEqual(in_port.get('1'), 1)
        self.assertIsNone(in_port.get('2'))
        self.assertIsNone(in_port.get('3'))

        in_port.port.add(2)
        in_port.read_new_events()
        self.assertIsNone(in_port.get())
        self.assertEqual(in_port.get('1'), 1)
        self.assertEqual(in_port.get('2'), 2)
        self.assertIsNone(in_port.get('3'))

        in_port.port.clear()
        in_port.port.add(2)
        in_port.port.add(3)
        in_port.read_new_events()
        self.assertIsNone(in_port.get())
        self.assertEqual(in_port.get('1'), 1)
        self.assertEqual(in_port.get('2'), 2)
        self.assertEqual(in_port.get('3'), 3)


if __name__ == '__main__':
    unittest.main()
