from perfdevs.models import Coupled
from perfdevs.sim import Coordinator
import time

from client_generator import ClientGenerator
from store_queue import StoreQueue
from employee import Employee


class StoreCashier(Coupled):
    def __init__(self, n_employees=10000, mean_employees=30, mean_clients=1, stddev_employees=0, stddev_clients=0,
                 name=None):
        super().__init__(name)

        generator = ClientGenerator(mean_clients, stddev_clients)
        queue = StoreQueue()

        self.add_component(generator)
        self.add_component(queue)
        self.add_coupling(generator.output_new_client, queue.input_new_client)
        for i in range(n_employees):
            employee = Employee(i, mean_employees, stddev_employees)
            self.add_component(employee)
            self.add_coupling(queue.output_client_to_employee, employee.input_client)
            self.add_coupling(employee.output_ready, queue.input_available_employee)


if __name__ == '__main__':

    sim_time = 30 * 60
    store = StoreCashier()
    start = time.time()
    coord = Coordinator(store, flatten=False, force_chain=False)
    coord.initialize()
    middle = time.time()
    print(middle - start)
    coord.simulate_time(sim_time)
    end = time.time()
    print(end - start)

