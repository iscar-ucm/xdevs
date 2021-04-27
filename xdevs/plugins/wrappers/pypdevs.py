import logging
from typing import NoReturn
from xdevs.models import Atomic, Port

try:
    from pypdevs.DEVS import AtomicDEVS
    from pypdevs.minimal import AtomicDEVS as AtomicDEVSMin
except ImportError:
    logging.warning("pypdevs module not installed.")


def update_sigma_on_state_change(delt_func):
    def inner(self, *args, **kwargs):
        prev_state = self.phase
        delt_func(self, *args, **kwargs)
        if prev_state != self.phase:
            self.sigma = self.atomic.timeAdvance()

    return inner


class PyPDEVSWrapper(Atomic):

    def __init__(self, atomic: AtomicDEVS or AtomicDEVSMin):
        super().__init__()
        self.atomic: AtomicDEVS or AtomicDEVSMin = atomic
        self.pypdevs_in_ports = {}  # IO ports dictionaries to efficiently manage pypdevs ports
        self.xdevs_out_ports = {}

        for pypdevs_in_port in self.atomic.IPorts:
            xdevs_in_port = Port(None, pypdevs_in_port.name)
            self.add_in_port(xdevs_in_port)
            self.pypdevs_in_ports[pypdevs_in_port.name] = pypdevs_in_port
            setattr(self, pypdevs_in_port.name, xdevs_in_port)

        for pypdevs_out_port in self.atomic.OPorts:
            xdevs_out_port = Port(None, pypdevs_out_port.name)
            self.add_out_port(xdevs_out_port)
            self.xdevs_out_ports[pypdevs_out_port.name] = xdevs_out_port
            setattr(self, pypdevs_out_port.name, xdevs_out_port)

    @update_sigma_on_state_change
    def deltint(self):
        self.phase = self.atomic.state = self.atomic.intTransition()

    @update_sigma_on_state_change
    def deltext(self, e: float):
        self.phase = self.atomic.state = self.atomic.extTransition(self._inputs_to_dict())
        self.continuef(e)

    def lambdaf(self) -> NoReturn:
        outputs = self.atomic.outputFnc()

        for pypdevs_out_port, values in outputs.items():
            if len(values) > 0:
                xdevs_out_port = self.xdevs_out_ports[pypdevs_out_port.name]
                xdevs_out_port.extend(values)

    def initialize(self) -> NoReturn:
        pass

    def exit(self) -> NoReturn:
        pass

    def _inputs_to_dict(self):
        in_values = {}

        for in_port in self.in_ports:
            in_port_values = list(in_port.values)
            in_values[self.pypdevs_in_ports[in_port.name]] = in_port_values

        return in_values
