import logging
from random import gauss
from perfdevs.models import Atomic, Port

from msg import NewClient


class ClientGeneratorStatus:
    def __init__(self):
        self.next_client_id = 0
        self.time_to_next = 0

    def __str__(self):
        return '<next: {}: in: {}>'.format(self.next_client_id, self.time_to_next)


class ClientGenerator(Atomic):

    def __init__(self, mean: float = 10, stddev: float = 0, name: str = None):
        super().__init__(name)
        self.mean = mean
        self.stddev = stddev
        self.clock = 0

        self.output_new_client = Port(NewClient)
        self.add_out_port(self.output_new_client)

    def deltint(self):
        self.clock += self.sigma
        self.phase.next_client_id += 1
        self.phase.time_to_next = max(gauss(self.mean, self.stddev), 0)
        logging.debug('({}) [{}]-> {}'.format(self.clock, self.name, str(self.phase)))
        self.hold_in(self.phase, self.phase.time_to_next)

    def deltext(self, e):
        pass

    def lambdaf(self):
        self.output_new_client.add(NewClient(self.phase, self.clock + self.sigma))

    def initialize(self):
        self.hold_in(ClientGeneratorStatus(), 0)

    def exit(self):
        pass
