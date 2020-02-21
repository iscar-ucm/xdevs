import unittest
from typing import Any

from perfdevs.models import Atomic, Coupled, Port


class DummyAtomic(Atomic):
    def __init__(self, n_in, n_out, name=None):
        super().__init__(name)
        for i in range(n_in):
            port = Port(int, 'IN' + str(i))
            self.add_in_port(port)
        for i in range(n_out):
            port = Port(int, 'OUT' + str(i))
            self.add_out_port(port)

    def deltint(self):
        pass

    def deltext(self, e: Any):
        pass

    def lambdaf(self):
        pass

    def initialize(self):
        pass

    def exit(self):
        pass


class DummyCoupled(Coupled):
    def __init__(self, n_ports, n_atomics, n_coupleds, name=None, to_chain=False):
        super().__init__(name, to_chain)

        for i in range(n_coupleds):
            coupled = DummyCoupled(n_ports, n_atomics, n_coupleds - 1, name + 'C' + str(i), to_chain)
            self.add_component(coupled)

        for i in range(n_atomics):
            atom = DummyAtomic(n_ports, n_ports, name + 'A' + str(i))
            self.add_component(atom)

        for i in range(len(self.components) - 1):
            for j in range(i + 1, len(self.components)):
                for k in range(n_ports):
                    self.add_coupling(self.components[i].out_ports[k], self.components[j].in_ports[k])

        for i in range(n_ports):
            port = Port(int, 'IN' + str(i))
            self.add_in_port(port)
            for comp in self.components:
                self.add_coupling(port, comp.in_ports[i])
        for i in range(n_ports):
            port = Port(int, 'OUT' + str(i))
            self.add_out_port(port)
            for comp in self.components:
                self.add_coupling(comp.out_ports[i], port)


class MyTestCase(unittest.TestCase):
    def test_flatten(self):
        n_ports = 2
        n_atomics = 2
        n_coupleds = 2
        comp = DummyCoupled(n_ports, n_atomics, n_coupleds, ' ', False)
        comp.flatten()
        self.assertEqual(True, False)


if __name__ == '__main__':
    unittest.main()
