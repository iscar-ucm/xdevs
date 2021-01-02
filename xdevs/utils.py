from typing import Any
from xdevs.models import Atomic, Port


class Generator(Atomic):

    def __init__(self, name, num_outputs=1, period=None):
        super(Generator, self).__init__(name=name)
        self.num_outputs = num_outputs
        self.period = period

        self.o_out = Port(int, "o_out")
        self.add_out_port(self.o_out)

    def deltint(self):
        self.hold_in("active", self.period) if self.period else self.passivate()

    def deltext(self, e: Any):
        pass

    def lambdaf(self):
        self.o_out.extend(range(self.num_outputs))

    def initialize(self):
        self.activate()

    def exit(self):
        pass
