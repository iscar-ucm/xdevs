from abc import ABC, abstractmethod
from typing import Any

from xdevs.examples.devstone.pystone import pystones
from xdevs.models import Atomic, Coupled, Port
from xdevs.sim import Coordinator, ParallelProcessCoordinator
from xdevs import PHASE_ACTIVE, get_logger
from collections import defaultdict

# logger = get_logger(__name__)
# logger.disabled = False


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
        self.int_count += 1
        #logger.debug(self.name + " int %d, init" % self.int_count)

        if self.int_delay:
            pystones(self.int_delay)

        #logger.debug(self.name + " int %d, end" % self.int_count)
        self.passivate()

    def deltext(self, e: Any):
        self.ext_count += 1
        #logger.debug(self.name + " ext %d, init" % self.ext_count)

        if self.ext_delay:
            pystones(self.ext_delay)

        #logger.debug(self.name + " ext %d, end" % self.ext_count)
        self.hold_in(PHASE_ACTIVE, self.prep_time)

    """def deltcon(self, e: Any):
        self.deltext(0)
        self.deltint()"""

    def lambdaf(self):
        # TODO: find out what to add in output
        if hasattr(self, "o_out"):
            #logger.debug(self.name + " lambda")
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


class HOmod(Coupled):

    def __init__(self, name: str, depth: int, width: int, int_delay: float, ext_delay: float):
        super().__init__(name)

        self.depth = depth
        self.width = width
        self.int_delay = int_delay
        self.ext_delay = ext_delay

        self.i_in = Port(int, "i_in")
        self.add_in_port(self.i_in)
        self.i_in2 = Port(int, "i_in2")
        self.add_in_port(self.i_in2)
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
            coupled = HOmod("Coupled_%d" % (self.depth - 1), self.depth - 1, self.width, self.int_delay, self.ext_delay)
            self.add_component(coupled)
            self.add_coupling(self.i_in, coupled.get_in_port("i_in"))
            self.add_coupling(coupled.get_out_port("o_out"), self.o_out)

            if width >= 2:
                atomics = defaultdict(list)

                # Generate atomic components
                for i in range(width):
                    min_row_idx = 0 if i < 2 else i - 1
                    for j in range(min_row_idx, width - 1):
                        atomic = DelayedAtomic("Atomic_%d_%d_%d" % (depth - 1, i, j), int_delay, ext_delay, add_out_port=True)
                        self.add_component(atomic)
                        atomics[i].append(atomic)

                # Connect EIC
                for atomic in atomics[0]:
                    self.add_coupling(self.i_in2, atomic.i_in)
                for i in range(1, width):
                    atomic_set = atomics[i]
                    self.add_coupling(self.i_in2, atomic_set[0].i_in)

                # Connect IC
                for atomic in atomics[0]:  # First row to coupled component
                    self.add_coupling(atomic.o_out, coupled.i_in2)
                for i in range(len(atomics[1])):  # Second to first rows
                    for j in range(len(atomics[0])):
                        self.add_coupling(atomics[1][i].o_out, atomics[0][j].i_in)
                for i in range(2, width):  # Rest of rows
                    for j in range(len(atomics[i])):
                        self.add_coupling(atomics[i][j].o_out, atomics[i-1][j+1].i_in)


if __name__ == '__main__':
    import sys
    sys.setrecursionlimit(10000)
    root = HO("HO_root", 50, 50, 0, 0)
    flatten = False
    parallel = False
    if parallel:
        coord = ParallelProcessCoordinator(root)
    else:
        coord = Coordinator(root, flatten=flatten)
    coord.initialize()
    coord.inject(root.i_in, 0)
    coord.simulate()
