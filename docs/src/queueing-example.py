import logging
import xdevs
from xdevs.models import Atomic, Port, Coupled
from xdevs.sim import Coordinator, SimulationClock

logger = xdevs.get_logger(__name__, logging.INFO)

class Customer:
    def __init__(self, id):
        self.id = id

class Generator(Atomic):
    def __init__(self, name, interarrival_time):
        super().__init__(name)
        self.interarrival_time = interarrival_time
        self.output_port = Port(Customer, "out")
        self.add_out_port(self.output_port)

    def initialize(self):
        self.customer_id = 1
        self.hold_in("active", self.interarrival_time)

    def deltint(self):
        self.customer_id += 1
        self.hold_in("active", self.interarrival_time)

    def lambdaf(self):
        logger.info(f"Generator::Arriving customer {self.customer_id} to queue.")
        self.output_port.add(Customer(self.customer_id))

    def deltext(self, e):
        pass

    def exit(self):
        pass

class Queue(Atomic):
    def __init__(self, name):
        super().__init__(name)
        self.input_request = Port(bool, "request")
        self.input_customer = Port(Customer, "in")
        self.output_customer = Port(Customer, "out")
        self.add_in_port(self.input_request)
        self.add_in_port(self.input_customer)
        self.add_out_port(self.output_customer)

    def initialize(self):
        self.queue = []
        self.customer_requested = False
        self.next_customer = None
        self.passivate()

    def deltint(self):
        self.customer_requested = False
        self.next_customer = None
        self.passivate()

    def deltext(self, e):
        self.continuef(e)            
        if self.input_customer:
            self.queue.append(self.input_customer.get())
            logger.info(f"Queue::Customer {self.queue[-1].id} in queue. Queue size: {len(self.queue)}")
            self.passivate()
        if self.input_request:
            self.customer_requested = True
            self.passivate()
        if self.customer_requested and not self.next_customer and self.queue:
            self.next_customer = self.queue.pop(0)
            self.hold_in("active", 0.1) # Small delay to simulate moving customer

    def lambdaf(self):
        logger.info(f"Queue::Customer {self.next_customer.id} from queue to server. Queue size: {len(self.queue)}")
        self.output_customer.add(self.next_customer)

    def exit(self):
        pass

class Server(Atomic):
    def __init__(self, name, service_time):
        super().__init__(name)
        self.service_time = service_time
        self.input_customer = Port(Customer, "in")
        self.output_request = Port(bool, "request")
        self.add_in_port(self.input_customer)
        self.add_out_port(self.output_request)

    def initialize(self):
        self.current_customer = None
        self.hold_in("request", 0.1)

    def deltint(self):
        if self.current_customer:
            logger.info(f"Server::Finished attending customer {self.current_customer.id}.")
            self.current_customer = None
            self.hold_in("request", 0.1)
        else:
            self.passivate()

    def deltext(self, e):
        self.continuef(e)
        if self.input_customer:
            if self.current_customer:
                logger.error(f"Server::ERROR: Server is already attending customer {self.current_customer.id}")
            else:
                self.current_customer = self.input_customer.get()
                logger.info(f"Server::Attending customer {self.current_customer.id}.")
                self.hold_in("busy", self.service_time)

    def lambdaf(self):
        if self.current_customer is None:
            logger.info(f"Server::Requesting customer.")
            self.output_request.add(True)

    def exit(self):
        pass

class QueueingSystem(Coupled):
    def __init__(self, name, interarrival_time, service_time):
        super().__init__(name)
        self.generator = Generator("Generator", interarrival_time)
        self.queue = Queue("Queue")
        self.server = Server("Server", service_time)

        self.add_component(self.generator)
        self.add_component(self.queue)
        self.add_component(self.server)

        self.add_coupling(self.generator.output_port, self.queue.input_customer)
        self.add_coupling(self.queue.output_customer, self.server.input_customer)
        self.add_coupling(self.server.output_request, self.queue.input_request)

if __name__ == "__main__":
    interarrival_time = 2.0
    service_time = 5.0

    queueing_system = QueueingSystem("QueueingSystem", interarrival_time, service_time)
    clock = SimulationClock()
    coordinator = Coordinator(queueing_system, clock)

    coordinator.initialize()
    coordinator.simulate_time(50)  # Simulate for 50 time units
