from math import inf
from queue import PriorityQueue
from typing import Dict, Generic, Optional
from xdevs.celldevs.inout import C, DelayedOutput, S


class TransportDelayedOutput(DelayedOutput[C, S], Generic[C, S]):
    def __init__(self, cell_id: C, serve: bool = False):
        super().__init__(cell_id, serve)
        self.last_state: Optional[S] = None
        self.schedule: PriorityQueue = PriorityQueue()
        self.next_states: Dict[float, S] = dict()

    def add_to_buffer(self, when: float, state: S):
        if when not in self.next_states:
            self.schedule.put(when)
        self.next_states[when] = state

    def next_time(self) -> float:
        return self.schedule.queue[0] if self.next_states else inf

    def next_state(self) -> S:
        return self.next_states[self.schedule.queue[0]] if self.next_states else self.last_state

    def pop_state(self):
        if not self.schedule.empty():
            self.last_state = self.next_states.pop(self.schedule.get())
