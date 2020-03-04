from abc import ABC, abstractmethod
from typing import Any

from perfdevs.examples.devstone.pystone import pystones
from perfdevs.models import Atomic, Coupled, Port
from perfdevs.sim import Coordinator, ParallelCoordinator
from perfdevs import PHASE_ACTIVE


class DelayedAtomic(Atomic):

    def __init__(self, name: str, int_delay: float, ext_delay: float, add_out_port: bool = False, prep_time=0):
        super().__init__(name)

        self.int_delay = int_delay
        self.ext_delay = ext_delay

        self.int_count = 0
        self.ext_count = 0

        self.prep_time = prep_time  # TODO What is this for?

        self.i_in = Port(int, "i_in")
        self.add_in_port(self.i_in)

        if add_out_port:
            self.o_out = Port(int, "o_out")
            self.add_out_port(self.o_out)

    def deltint(self):
        #print(self.name + " int")
        self.int_count += 1

        if self.int_delay:
            pystones(self.int_delay)
        self.passivate()

    def deltext(self, e: Any):
        #print(self.name + " ext")
        self.ext_count += 1

        if self.ext_delay:
            pystones(self.ext_delay)
        self.hold_in(PHASE_ACTIVE, self.prep_time)

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
        if int_delay < 0:
            raise ValueError("Invalid int_delay")
        if ext_delay < 0:
            raise ValueError("Invalid ext_delay")

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

        assert len(self.components) > 0
        if isinstance(self.components[0], Coupled):
            self.add_coupling(self.i_in, self.components[0].get_in_port("i_in2"))  # Coupled second input

        if len(self.components) > 1:
            assert isinstance(self.components[-1], Atomic)
            self.add_coupling(self.i_in2, self.components[-1].get_in_port("i_in"))
            self.add_coupling(self.components[-1].get_out_port("o_out"), self.o_out2)

        for idx in range(1, len(self.components) - 1):
            assert isinstance(self.components[idx], Atomic)
            self.add_coupling(self.components[idx].get_out_port("o_out"), self.components[idx + 1].get_in_port("i_in"))
            self.add_coupling(self.i_in2, self.components[idx].get_in_port("i_in"))
            self.add_coupling(self.components[idx].get_out_port("o_out"), self.o_out2)

    def gen_coupled(self):
        return HO("Coupled_%d" % (self.depth - 1), self.depth - 1, self.width, self.int_delay, self.ext_delay)


if __name__ == '__main__':
    import sys
    sys.setrecursionlimit(10000)
    root = HO("HO_root", 100, 30, 0, 0)
    flatten = False
    chain = True
    parallel = False
    if parallel:
        coord = ParallelCoordinator(root, flatten=flatten, chain=chain)
    else:
        coord = Coordinator(root, flatten=flatten, chain=chain)
    coord.initialize()
    coord.inject(root.i_in, 0)
    coord.simulate()
