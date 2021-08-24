from math import inf
from typing import Generic, Optional
from xdevs.celldevs.inout import C, DelayedOutput, S


class InertialDelayedOutput(DelayedOutput[C, S], Generic[C, S]):
    def __init__(self, cell_id: C, serve: bool = False):
        super().__init__(cell_id, serve)
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
