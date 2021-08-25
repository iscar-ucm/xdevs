from __future__ import annotations
from typing import Dict, Type
from xdevs.celldevs.cell import S
from xdevs.celldevs.grid import C, GridCell, GridCellConfig
from xdevs.transducers import Transducible, T


class State(Transducible):
    def __init__(self, population: int, susceptible: float, infected: float, recovered: float):
        self.population: int = population
        self.susceptible: float = susceptible
        self.infected: float = infected
        self.recovered: float = recovered

    def __eq__(self, other: State):
        return self.population == other.population and self.susceptible == other.susceptible \
            and self.infected == other.infected and self.recovered == other.recovered

    @classmethod
    def transducible_fields(cls) -> Dict[str, Type[T]]:
        return {'population': int, 'susceptible': float, 'infected': float, 'recovered': float}

    def transduce(self) -> Dict[str, T]:
        return {'population': self.population, 'susceptible': self.susceptible,
                'infected': self.infected, 'recovered': self.recovered}


class Vicinity:
    def __init__(self, connectivity: float, mobility: float):
        self.connectivity: float = connectivity
        self.mobility: float = mobility

    @property
    def correlation(self) -> float:
        return self.connectivity * self.mobility


class Config:
    def __init__(self, virulence: float, recovery: float):
        self.virulence = virulence
        self.recovery = recovery


class SIRGridCell(GridCell[State, Vicinity]):
    def __init__(self, cell_id: C, config: GridCellConfig):
        super().__init__(cell_id, config)
        self.config: Config = Config(**config.cell_config)

    def local_computation(self, cell_state: S) -> S:
        new_infections = self.new_infections(cell_state)
        new_recoveries = self.new_recoveries(cell_state)
        cell_state.recovered = round((cell_state.recovered + new_recoveries) * 100) / 100
        cell_state.infected = round((cell_state.infected + new_infections - new_recoveries) * 100) / 100
        cell_state.susceptible = 1 - cell_state.infected - cell_state.recovered
        return cell_state

    def new_infections(self, state: State) -> float:
        neighbor_effect = sum(state.infected * state.population * self.neighborhood[neighbor].correlation
                              for neighbor, state in self.neighbors_state.items())
        new_infections = state.susceptible * self.config.virulence * neighbor_effect / state.population
        return min(state.susceptible, new_infections)

    def new_recoveries(self, state: State) -> float:
        return state.infected * self.config.recovery

    def output_delay(self, cell_state: S) -> float:
        return 1
