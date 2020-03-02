import sys, unittest
from typing import Any
from io import StringIO
import logging

from perfdevs.models import Atomic, Coupled, Port
from perfdevs.sim import Coordinator, get_logger
from perfdevs.examples.basic.basic import Gpt, logger as basic_logger

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
        n_coupleds = 1
        comp = DummyCoupled(n_ports, n_atomics, n_coupleds, ' ', False)
        comp.flatten()

        # Compute the expected amount of components of the flattened version
        n_components = 0
        n_ics = 0
        for i in range(n_coupleds + 1):
            n_components *= i
            n_ics *= i
            n_components += n_atomics
            n_ics += n_ports * sum(range(n_atomics))

        self.assertEqual(len(comp.components), n_components)
        self.assertEqual(len(comp.eic), n_ports * n_components)
        self.assertEqual(len(comp.eoc), n_ports * n_components)
        self.assertEqual(len(comp.ic), )

    def test_pure_chain(self):
        n_ports = 2
        n_atomics = 2
        n_coupleds = 2
        comp = DummyCoupled(n_ports, n_atomics, n_coupleds, ' ', False)
        comp.chain_components(force=True)
        self.assertEqual(True, False)

    def test_basic_behavior(self):
        gpt = Gpt("gpt", 3, 1000)
        coord = Coordinator(gpt, flatten=False, force_chain=False)
        coord.initialize()

        s = StringIO()
        handler = logging.StreamHandler(s)
        handler.setLevel(logging.INFO)
        basic_logger.addHandler(handler)

        coord.simulate()

        s.seek(0)
        with open("test_perfdevs/basic_3_100_ff.log", "r") as log_file:
            self.assertEqual(s.read().strip(), log_file.read().strip())



if __name__ == '__main__':
    unittest.main()
