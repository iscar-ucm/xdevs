from _collections import deque
from perfdevs.models import Atomic, Port, INFINITY
import logging

from msg import NewClient, ClientToEmployee


class QueueStatus:
    def __init__(self):
        self.clients = deque()
        self.employees = deque()
        self.pairings = deque()

    def __str__(self):
        return '<clients: {}; employees: {}>'.format(len(self.clients), len(self.employees))


class StoreQueue(Atomic):

    def __init__(self, name: str = None):
        super().__init__(name)
        self.clock = 0

        self.input_new_client = Port(NewClient)
        self.input_available_employee = Port(int)
        self.add_in_port(self.input_new_client)
        self.add_in_port(self.input_available_employee)

        self.output_client_to_employee = Port(ClientToEmployee)
        self.add_out_port(self.output_client_to_employee)

    def deltint(self):
        self.clock += self.ta
        self.phase.pairings.clear()
        self.hold_in(self.phase, INFINITY)

    def deltext(self, e):
        self.clock += e
        for employee_id in self.input_available_employee.values:
            try:
                self.phase.pairings.appendleft(ClientToEmployee(self.phase.clients.pop(), employee_id))
            except IndexError:
                self.phase.employees.appendleft(employee_id)
        for new_client in self.input_new_client.values:
            try:
                self.phase.pairings.appendleft(ClientToEmployee(new_client, self.phase.employees.pop()))
            except IndexError:
                self.phase.clients.appendleft(new_client)
        logging.debug('({}) [{}]-> {}'.format(self.clock, self.name, str(self.phase)))
        timeout = 0 if self.phase.pairings else INFINITY
        self.hold_in(self.phase, timeout)

    def lambdaf(self):
        for pairing in self.phase.pairings:
            self.output_client_to_employee.add(pairing)

    def initialize(self):
        self.hold_in(QueueStatus(), INFINITY)

    def exit(self):
        pass
