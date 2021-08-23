from math import inf
from unittest import TestCase
from xdevs.celldevs.inout import DelayedOutput, DelayedOutputs


class TestCellDEVSDelays(TestCase):
    def test_inertial_delay(self):
        delay: DelayedOutput[int] = DelayedOutputs.create_celldevs_delay('inertial')
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        delay.pop_state()
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        n = 10
        for i in range(1, n + 1):
            delay.add_to_buffer(i * 4, i)
            self.assertEqual(i, delay.next_state())
            self.assertEqual(i * 4, delay.next_time())
        delay.pop_state()
        self.assertEqual(n, delay.next_state())
        self.assertEqual(inf, delay.next_time())

    def test_transport_delay(self):
        delay = DelayedOutputs.create_celldevs_delay('transport')
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        delay.pop_state()
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        n = 10

        for i in range(1, n + 1):
            delay.add_to_buffer(i * 4, i)
            self.assertEqual(1, delay.next_state())
            self.assertEqual(4, delay.next_time())
        for i in range(1, n + 1):
            self.assertEqual(i, delay.next_state())
            self.assertEqual(i * 4, delay.next_time())
            delay.pop_state()
        self.assertEqual(n, delay.next_state())
        self.assertEqual(inf, delay.next_time())

        for i in range(n, -1, -1):
            for j in range(1, i + 1):
                delay.add_to_buffer(i * 4, j)
                self.assertEqual(j, delay.next_state())
                self.assertEqual(i * 4, delay.next_time())
        for i in range(1, n + 1):
            self.assertEqual(i, delay.next_state())
            self.assertEqual(i * 4, delay.next_time())
            delay.pop_state()
        self.assertEqual(n, delay.next_state())
        self.assertEqual(inf, delay.next_time())

    def test_hybrid_delay(self):
        delay = DelayedOutputs.create_celldevs_delay('hybrid')
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        delay.pop_state()
        self.assertIsNone(delay.next_state())
        self.assertEqual(inf, delay.next_time())

        n = 10

        for i in range(1, n + 1):
            delay.add_to_buffer(i * 4, i)
            self.assertEqual(1, delay.next_state())
            self.assertEqual(4, delay.next_time())
        for i in range(1, n + 1):
            self.assertEqual(i, delay.next_state())
            self.assertEqual(i * 4, delay.next_time())
            delay.pop_state()
        self.assertEqual(n, delay.next_state())
        self.assertEqual(inf, delay.next_time())

        for i in range(n, -1, -1):
            for j in range(1, i + 1):
                delay.add_to_buffer(i * 4, j)
                self.assertEqual(j, delay.next_state())
                self.assertEqual(i * 4, delay.next_time())
        self.assertEqual(1, delay.next_state())
        self.assertEqual(4, delay.next_time())
        delay.pop_state()

        self.assertEqual(1, delay.next_state())
        self.assertEqual(inf, delay.next_time())
