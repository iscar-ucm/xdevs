from collections import deque
from math import inf
from typing import Deque, Generic, Optional, Tuple
from xdevs.celldevs.delay import CellDEVSDelay, T


class HybridCellDEVSDelay(Generic[T], CellDEVSDelay[T]):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.last_event: Optional[T] = None
        self.next_events: Deque[Tuple[float, T]] = deque()

    def add_to_buffer(self, when: float, event: T):
        while self.next_events and self.next_events[-1][0] >= when:
            self.next_events.pop()
        self.next_events.append((when, event))

    def next_time(self) -> float:
        return inf if not self.next_events else self.next_events[0][0]

    def next_event(self) -> T:
        return self.last_event if not self.next_events else self.next_events[0][1]

    def pop_event(self):
        if self.next_events:
            self.last_event = self.next_events.popleft()[1]
