from collections import deque
from math import inf
from typing import Deque, Generic, Optional, Tuple
from xdevs.celldevs.inout import DelayedOutput, S


class HybridDelayedOutput(Generic[S], DelayedOutput[S]):
    def __init__(self):
        super().__init__()
        self.last_state: Optional[S] = None
        self.next_states: Deque[Tuple[float, S]] = deque()

    def add_to_buffer(self, when: float, state: S):
        while self.next_states and self.next_states[-1][0] >= when:
            self.next_states.pop()
        self.next_states.append((when, state))

    def next_time(self) -> float:
        return inf if not self.next_states else self.next_states[0][0]

    def next_state(self) -> S:
        return self.last_state if not self.next_states else self.next_states[0][1]

    def pop_state(self):
        if self.next_states:
            self.last_state = self.next_states.popleft()[1]
