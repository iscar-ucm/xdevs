from math import inf
from queue import PriorityQueue
from typing import Dict, Generic, Optional
from xdevs.celldevs.delay import CellDEVSDelay, T


class TransportCellDEVSDelay(Generic[T], CellDEVSDelay[T]):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.last_event: Optional[T] = None
        self.schedule: PriorityQueue = PriorityQueue()
        self.next_events: Dict[float, T] = dict()

    def add_to_buffer(self, when: float, event: T):
        if when not in self.next_events:
            self.schedule.put(when)
        self.next_events[when] = event

    def next_time(self) -> float:
        return self.schedule.queue[0] if self.next_events else inf

    def next_event(self) -> T:
        return self.next_events[self.schedule.queue[0]] if self.next_events else self.last_event

    def pop_event(self):
        if not self.schedule.empty():
            self.last_event = self.next_events.pop(self.schedule.get())
