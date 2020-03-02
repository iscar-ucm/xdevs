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

        stream = StringIO()
        handler = logging.StreamHandler(stream)
        handler.setLevel(logging.INFO)
        basic_logger.removeHandler(basic_logger.handlers[0])
        basic_logger.addHandler(handler)

        for params_tuple in [(1, 1), (1, 500), (3, 1000)]:
            params = dict(zip(("period", "obs_time"), params_tuple))

            with self.subTest(**params):
                stream.truncate(0)
                stream.seek(0)

                gpt = Gpt("gpt", params["period"], params["obs_time"])
                coord = Coordinator(gpt, flatten=False, force_chain=False)
                coord.initialize()
                coord.simulate()

                stream.seek(0)

                with open("test_perfdevs/basic_%d_%d.log" % (params["period"], params["obs_time"]), "r") as log_file:
                    self.assertEqual(stream.read().strip(), log_file.read().strip())

    def test_basic_invalid_params(self):
        self.assertRaises(ValueError, Gpt, "gpt", -1, 100)
        self.assertRaises(ValueError, Gpt, "gpt", 10, -1)
        self.assertRaises(ValueError, Gpt, "gpt", -999, -999)
        self.assertRaises(ValueError, Gpt, "gpt", 0, 1e3)
