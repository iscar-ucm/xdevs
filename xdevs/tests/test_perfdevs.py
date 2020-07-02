import logging
import unittest
from typing import Any

from xdevs.models import Atomic, Coupled, Port
from io import StringIO
from xdevs.examples.basic.basic import Gpt, logger as basic_logger
from xdevs.sim import Coordinator, ParallelCoordinator


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
    def __init__(self, n_ports, n_atomics, n_coupleds, name=None):
        super().__init__(name)

        for i in range(n_coupleds):
            coupled = DummyCoupled(n_ports, n_atomics, n_coupleds - 1, name + 'C' + str(i))
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
        n_atomics = 3
        n_coupleds = 2
        comp = DummyCoupled(n_ports, n_atomics, n_coupleds, ' ')
        comp.flatten()

        # Compute the expected amount of components of the flattened version
        n_components = 0
        n_ics = 0
        for i in range(n_coupleds + 1):
            n_components *= i
            n_components += n_atomics
        for j in range(n_components):
            n_ics += n_ports * j

        self.assertEqual(len(comp.components), n_components)
        self.assertEqual(sum([len(comp.eic[cl]) for cl in comp.eic]), n_ports * n_components)
        self.assertEqual(sum([len(comp.eoc[cl]) for cl in comp.eoc]), n_ports * n_components)
        self.assertEqual(sum([len(comp.ic[cl]) for cl in comp.ic]), n_ics)

    def test_pure_chain(self):
        n_ports = 2
        n_atomics = 2
        n_coupleds = 2
        comp = DummyCoupled(n_ports, n_atomics, n_coupleds, ' ')
        comp.chain_components()

    def _test_basic_behavior(self, coord_type):

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
                coord = coord_type(gpt, flatten=False, chain=False)
                coord.initialize()
                coord.simulate()

                stream.seek(0)

                with open("xdevs/tests/test_xdevs/basic_%d_%d.log" % (params["period"], params["obs_time"]), "r") as log_file:
                    self.assertEqual(stream.read().strip(), log_file.read().strip())

    def test_basic_behavior_sequential(self):
        self._test_basic_behavior(Coordinator)

    def test_basic_behavior_parallel(self):
        self._test_basic_behavior(ParallelCoordinator)

    def test_basic_invalid_params(self):
        self.assertRaises(ValueError, Gpt, "gpt", -1, 100)
        self.assertRaises(ValueError, Gpt, "gpt", 10, -1)
        self.assertRaises(ValueError, Gpt, "gpt", -999, -999)
        self.assertRaises(ValueError, Gpt, "gpt", 0, 1e3)
