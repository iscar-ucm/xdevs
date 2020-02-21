from abc import ABC, abstractmethod
from typing import Any

from perfdevs.models import Atomic, Coupled, Port
from perfdevs.sim import Coordinator
from perfdevs import PHASE_ACTIVE, PHASE_PASSIVE
import typing


class DelayedAtomic(Atomic):

    def __init__(self, name: str, int_delay: float, ext_delay: float, add_out_port: bool = False):
        super().__init__(name)

        self.int_delay = int_delay
        self.ext_delay = ext_delay

        self.int_count = 0
        self.ext_count = 0

        self.i_in = Port(int, "i_in")
        self.add_in_port(self.i_in)

        if add_out_port:
            self.o_out = Port(int, "o_out")
            self.add_out_port(self.o_out)

    def deltint(self):
        self.int_count += 1

        # TODO: Dhrystone call with int_delay
        self.passivate()

    def deltext(self, e: Any):
        self.ext_count += 1

        # TODO: Dhrystone call with ext_delay
        self.hold_in(PHASE_ACTIVE, 0)  # TODO: Change to preparation time

    """def deltcon(self, e: Any):
        self.deltext(0)
        self.deltint()"""

    def lambdaf(self):
        # TODO: find out what to add in output
        if hasattr(self, "o_out"):
            self.o_out.add(0)

    def initialize(self):
        self.passivate()

    def exit(self):
        pass


class DEVStoneWrapper(Coupled, ABC):

    def __init__(self, name: str, depth: int, width: int, int_delay: float, ext_delay: float,
                 add_atomic_out_ports: bool = False):
        super().__init__(name)

        self.depth = depth
        self.width = width
        self.int_delay = int_delay
        self.ext_delay = ext_delay

        self.i_in = Port(int, "i_in")
        self.add_in_port(self.i_in)
        self.o_out = Port(int, "o_out")
        self.add_out_port(self.o_out)

        if depth < 1:
            raise ValueError("Invalid depth")
        if width < 1:
            raise ValueError("Invalid width")

        if depth == 1:
            atomic = DelayedAtomic("Atomic_0_0", int_delay, ext_delay, add_out_port=True)
            self.add_component(atomic)

            self.add_coupling(self.i_in, atomic.get_in_port("i_in"))
            self.add_coupling(atomic.get_out_port("o_out"), self.o_out)
        else:
            coupled = self.gen_coupled()
            self.add_component(coupled)
            self.add_coupling(self.i_in, coupled.get_in_port("i_in"))
            self.add_coupling(coupled.get_out_port("o_out"), self.o_out)

            for idx in range(width - 1):
                atomic = DelayedAtomic("Atomic_%d_%d" % (depth - 1, idx), int_delay, ext_delay,
                                       add_out_port=add_atomic_out_ports)
                self.add_component(atomic)

    @abstractmethod
    def gen_coupled(self):
        """ :return a coupled method with i_in and o_out ports"""
        pass


class LI(DEVStoneWrapper):

    def __init__(self, name: str, depth: int, width: int, int_delay: float, ext_delay: float):
        super().__init__(name, depth, width, int_delay, ext_delay, add_atomic_out_ports=False)

        for idx in range(1, len(self.components)):
            assert isinstance(self.components[idx], Atomic)
            self.add_coupling(self.i_in, self.components[idx].get_in_port("i_in"))

    def gen_coupled(self):
        return LI("Coupled_%d" % (self.depth - 1), self.depth - 1, self.width, self.int_delay, self.ext_delay)


class HI(DEVStoneWrapper):

    def __init__(self, name: str, depth: int, width: int, int_delay: float, ext_delay: float):
        super().__init__(name, depth, width, int_delay, ext_delay, add_atomic_out_ports=True)

        if len(self.components) > 1:
            assert isinstance(self.components[-1], Atomic)
            self.add_coupling(self.i_in, self.components[-1].get_in_port("i_in"))

        for idx in range(1, len(self.components) - 1):
            assert isinstance(self.components[idx], Atomic)
            self.add_coupling(self.components[idx].get_out_port("o_out"), self.components[idx + 1].get_in_port("i_in"))
            self.add_coupling(self.i_in, self.components[idx].get_in_port("i_in"))

    def gen_coupled(self):
        return HI("Coupled_%d" % (self.depth - 1), self.depth - 1, self.width, self.int_delay, self.ext_delay)


class HO(DEVStoneWrapper):

    def __init__(self, name: str, depth: int, width: int, int_delay: float, ext_delay: float):
        super().__init__(name, depth, width, int_delay, ext_delay, add_atomic_out_ports=True)

        self.i_in2 = Port(int, "i_in2")
        self.add_in_port(self.i_in2)
        self.o_out2 = Port(int, "o_out2")
        self.add_out_port(self.o_out2)

        if len(self.components) > 1:
            assert isinstance(self.components[-1], Atomic)
            self.add_coupling(self.i_in2, self.components[-1].get_in_port("i_in"))
            self.add_coupling(self.components[-1].get_out_port("o_out"), self.o_out2)

            assert isinstance(self.components[0], Coupled)
            self.add_coupling(self.i_in, self.components[0].get_in_port("i_in2"))  # Coupled second input

        for idx in range(1, len(self.components) - 1):
            assert isinstance(self.components[idx], Atomic)
            self.add_coupling(self.components[idx].get_out_port("o_out"), self.components[idx + 1].get_in_port("i_in"))
            self.add_coupling(self.i_in2, self.components[idx].get_in_port("i_in"))
            self.add_coupling(self.components[idx].get_out_port("o_out"), self.o_out2)

    def gen_coupled(self):
        return HO("Coupled_%d" % (self.depth - 1), self.depth - 1, self.width, self.int_delay, self.ext_delay)


if __name__ == '__main__':
    root = HO("HO_root", 3, 4, 100, 50)
    coord = Coordinator(root, flatten=False, force_chain=False)
    coord.initialize()
    coord.simulate()
