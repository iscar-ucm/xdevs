from math import inf
from typing import Generic, Optional
from xdevs.celldevs.inout import DelayedOutput, S


class InertialDelayedOutput(Generic[S], DelayedOutput[S]):
    def __init__(self):
        super().__init__()
        self.last_state: Optional[S] = None
        self.next_t: float = inf

    def add_to_buffer(self, when: float, state: S):
        self.next_t, self.last_state = when, state

    def next_time(self) -> float:
        return self.next_t

    def next_state(self) -> S:
        return self.last_state

    def pop_state(self):
        self.next_t = inf
