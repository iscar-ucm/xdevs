import logging
from random import gauss
from perfdevs.models import Atomic, Port, INFINITY

from msg import ClientToEmployee, LeavingClient


class EmployeeState:
    def __init__(self):
        self.clients_so_far = 0
        self.client = None
        self.time_remaining = 0

    def __str__(self):
        return '<so far: {}; busy: {}; for: {}>'.format(self.clients_so_far, bool(self.client), self.time_remaining)


class Employee(Atomic):

    def __init__(self, employee_id: int, mean: float = 30, stddev: float = 0, name: str = None):
        super().__init__(name)
        self.name += str(employee_id)
        self.employee_id = employee_id
        self.mean = mean
        self.stddev = stddev
        self.clock = 0

        self.input_client = Port(ClientToEmployee)
        self.add_in_port(self.input_client)

        self.output_ready = Port(int)
        self.output_client = Port(LeavingClient)
        self.add_out_port(self.output_ready)
        self.add_out_port(self.output_client)

    def deltint(self):
        self.clock += self.sigma
        self.phase.client = None
        self.phase.time_remaining = INFINITY
        self.hold_in(self.phase, self.phase.time_remaining)

    def deltext(self, e):
        self.clock += e
        if self.phase.client is not None:
            self.phase.time_remaining -= e
        else:
            for pairing in self.input_client.values:
                if pairing.employee_id == self.employee_id:
                    self.phase.clients_so_far += 1
                    self.phase.client = pairing.client
                    self.phase.time_remaining = max(gauss(self.mean, self.stddev), 0)
                    logging.debug('({}) [{}]-> {}'.format(self.clock, self.name, str(self.phase)))
        self.hold_in(self.phase, self.phase.time_remaining)

    def lambdaf(self):
        clock = self.clock + self.phase.time_remaining
        if self.phase.client is not None:
            self.output_client.add(LeavingClient(self.phase.client.client_id, self.phase.client.t_entered, clock))
        self.output_ready.add(self.employee_id)

    def initialize(self):
        self.hold_in(EmployeeState(), 0)

    def exit(self):
        pass
