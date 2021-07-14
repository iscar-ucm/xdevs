from math import inf
from typing import Generic, Optional
from xdevs.celldevs.delay import CellDEVSDelay, T


class InertialCellDEVSDelay(Generic[T], CellDEVSDelay[T]):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.last_event: Optional[T] = None
        self.next_t: float = inf

    def add_to_buffer(self, when: float, event: T):
        self.next_t, self.last_event = when, event

    def next_time(self) -> float:
        return self.next_t

    def next_event(self) -> T:
        return self.last_event

    def pop_event(self):
        self.next_t = inf
