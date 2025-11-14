from xdevs.models import Atomic, Port, Coupled
from xdevs.sim import Coordinator, SimulationClock

# Define the states
STATE_GREEN = "Green"
STATE_YELLOW = "Yellow"
STATE_RED = "Red"

class TrafficLight(Atomic):
    def __init__(self, name: str, green_time: float, yellow_time: float, red_time: float):
        super().__init__(name)
        self.green_time = green_time
        self.yellow_time = yellow_time
        self.red_time = red_time

        self.state = STATE_GREEN
        self.sigma = green_time

        self.output_port = Port(str, "state")
        self.add_out_port(self.output_port)

    def initialize(self):
        self.hold_in(STATE_GREEN, self.green_time)

    def deltint(self):
        if self.state == STATE_GREEN:
            self.state = STATE_YELLOW
            self.hold_in(STATE_YELLOW, self.yellow_time)
        elif self.state == STATE_YELLOW:
            self.state = STATE_RED
            self.hold_in(STATE_RED, self.red_time)
        elif self.state == STATE_RED:
            self.state = STATE_GREEN
            self.hold_in(STATE_GREEN, self.green_time)

    def deltext(self, e):
        pass

    def lambdaf(self):
        self.output_port.add(self.state)

    def exit(self):
        pass

# Simulation setup
if __name__ == "__main__":
    green_time = 10.0
    yellow_time = 2.0
    red_time = 8.0

    traffic_light = TrafficLight("TrafficLight", green_time, yellow_time, red_time)
    coupled = Coupled("TrafficLightSystem")
    coupled.add_component(traffic_light)
    clock = SimulationClock()
    coordinator = Coordinator(coupled, clock)

    coordinator.initialize()
    coordinator.simulate(50)  # Simulate for 50 iterations
