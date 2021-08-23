import math
from abc import ABC
from math import copysign, isinf
from typing import Dict, Generic, Iterator, List, Optional, Tuple, Type, TypeVar, Union
from xdevs.celldevs.cell import Cell, CellConfig

C = Tuple[int, ...]  # Cell IDs in grids are tuples of integers
S = TypeVar('S')     # Variable type used for cell states
V = TypeVar('V')     # Variable type used for cell vicinities


class GridScenario:
    def __init__(self, shape: C, origin: Optional[C] = None, wrapped: bool = False):
        for dim in shape:
            if dim <= 0:
                raise ValueError('scenario shape is invalid')
        self.shape = shape
        if origin is None:
            origin = tuple(0 for _ in range(self.dimension))
        if len(origin) != len(shape):
            raise ValueError('scenario shape and origin must have the same dimension')
        self.origin = origin
        self.wrapped = wrapped

    @property
    def dimension(self) -> int:
        return len(self.shape)

    def cell_in_scenario(self, cell: C) -> bool:
        return self._cell_in_scenario(cell, self.shape, self.origin)

    def distance_vector(self, cell_from: C, cell_to: C) -> C:
        return self._distance_vector(cell_from, cell_to, self.shape, self.origin, self.wrapped)

    def cell_to(self, cell_from: C, distance_vector: C):
        if not self.cell_in_scenario(cell_from):
            raise ValueError('cell_from is not part of the scenario')
        elif len(distance_vector) != self.dimension:
            raise ValueError('scenario shape and distance_vector must have the same dimension')
        cell_to: C = tuple(cell_from[i] + distance_vector[i] for i in range(self.dimension))
        if self.wrapped:
            cell_to = tuple((cell_to[i] + self.shape[i]) % self.shape[i] for i in range(self.dimension))
        if not self.cell_in_scenario(cell_to):
            raise OverflowError('cell_to is not part of the scenario')
        return cell_to

    def minkowski_distance(self, p: int, cell_from: C, cell_to: C) -> Union[int, float]:
        return self._minkowski_distance(p, cell_from, cell_to, self.shape, self.origin, self.wrapped)

    def moore_neighborhood(self, r: int) -> List[C]:
        return self._moore_neighborhood(self.dimension, r)

    def von_neumann_neighborhood(self, r: int) -> List[C]:
        return self._von_neumann_neighborhood(self.dimension, r)

    def next_cell(self, prev_cell: C) -> Optional[C]:
        return self._next_cell(prev_cell, self.shape, self.origin, 0)

    def iter_cells(self) -> Iterator[C]:
        return self._iter_cells(self.shape, self.origin)

    @staticmethod
    def _cell_in_scenario(cell: C, shape: C, origin: C) -> bool:
        if len(cell) != len(shape):
            raise ValueError('scenario shape and cell location must have the same dimension')
        return all(0 <= cell[i] - origin[i] < shape[i] for i in range(len(shape)))

    @classmethod
    def _distance_vector(cls, cell_from: C, cell_to: C, shape: C, origin: C, wrapped: bool) -> C:
        if not cls._cell_in_scenario(cell_from, shape, origin) or not cls._cell_in_scenario(cell_to, shape, origin):
            raise ValueError('cell_from and/or cell_to are not part of the scenario')
        dimension = len(shape)
        distance: C = tuple(cell_to[i] - cell_from[i] for i in range(dimension))
        if wrapped:
            distance = tuple(distance[i] - copysign(shape[i], distance[i]) if abs(distance[i]) > shape[i] / 2
                             else distance[i] for i in range(dimension))
        return distance

    @classmethod
    def _minkowski_distance(cls, p: int, cell_from: C, cell_to: C, shape: C,
                            origin: C, wrapped: bool) -> Union[int, float]:
        if p <= 0:
            raise ValueError('Minkowski distance is only valid for p greater than 0')
        d_vector: C = cls._distance_vector(cell_from, cell_to, shape, origin, wrapped)
        if p == 1:
            return sum(abs(d) for d in d_vector)
        elif isinf(p):
            return max(abs(d) for d in d_vector)
        else:
            return sum(abs(d) ** p for d in d_vector) ** (1 / p)

    @classmethod
    def _moore_neighborhood(cls, dim: int, r: int) -> List[C]:
        if dim < 0:
            raise ValueError('invalid number of dimensions')
        if r < 0:
            raise ValueError('neighborhood range must be greater than or equal to 0')
        n_shape: C = tuple(2 * r + 1 for _ in range(dim))
        n_origin: C = tuple(-r for _ in range(dim))
        neighborhood: List[C] = list()
        cell: Optional[C] = n_origin
        while cell is not None:
            neighborhood.append(cell)
            cell = cls._next_cell(cell, n_shape, n_origin, 0)
        return neighborhood

    @classmethod
    def _von_neumann_neighborhood(cls, dim: int, r: int) -> List[C]:
        moore: List[C] = cls._moore_neighborhood(dim, r)
        n_shape: C = tuple(2 * r + 1 for _ in range(dim))
        n_origin: C = tuple(-r for _ in range(dim))
        center: C = tuple(0 for _ in range(dim))
        neighborhood: List[C] = list()
        for neighbor in moore:
            if cls._minkowski_distance(1, center, neighbor, n_shape, n_origin, False) <= r:
                neighborhood.append(neighbor)
        return neighborhood

    @classmethod
    def _next_cell(cls, prev_cell: C, shape: C, origin: C, d: int) -> Optional[C]:
        if cls._cell_in_scenario(prev_cell, shape, origin):
            if prev_cell[d] - origin[d] < shape[d]:
                return tuple(prev_cell[i] if i != d else prev_cell[i] + 1 for i in range(len(shape)))
            elif d < len(shape) - 1:
                prev_cell = tuple(prev_cell[i] if i != d else 0 for i in range(len(shape)))
                return cls._next_cell(prev_cell, shape, origin, d + 1)

    @classmethod
    def _iter_cells(cls, shape: C, origin: C) -> Iterator[C]:
        cell: C = origin
        while cell is not None:
            yield cell
            cell = cls._next_cell(cell, shape, origin, 0)


class GridCellConfig(CellConfig[C, S, V], Generic[S, V]):
    def __init__(self, scenario: GridScenario, config_id: str, s_type: Type[S], v_type: Type[V], **kwargs):
        self.scenario: GridScenario = scenario
        super().__init__(config_id, tuple, s_type, v_type, **kwargs)

    def _load_map(self, *args) -> List[C]:
        return [tuple(cell_id) for cell_id in args]

    def load_cell_neighborhood(self, cell: C) -> Dict[C, V]:
        neighbors: Dict[C, V] = dict()
        for neighborhood in self.raw_neighborhood:
            vicinity = neighborhood.get('vicinity')
            n_type: str = neighborhood.get('type', 'absolute')
            if n_type == 'absolute':
                for neighbor in neighborhood.get('neighbors', list()):
                    neighbor = tuple(neighbor)
                    if not self.scenario.cell_in_scenario(neighbor):
                        raise OverflowError('absolute neighbor is not part of the scenario')
                    neighbors[neighbor] = self._load_vicinity(vicinity)
            else:
                if n_type == 'relative':
                    relative: List[C] = [tuple(neighbor) for neighbor in neighborhood.get('neighbors', list())]
                elif n_type == 'moore':
                    relative: List[C] = self.scenario.moore_neighborhood(neighborhood.get('range', 1))
                elif n_type == 'von_neumann':
                    relative: List[C] = self.scenario.von_neumann_neighborhood(neighborhood.get('range', 1))
                else:
                    raise ValueError('unknown neighborhood type')
                for neighbor in relative:
                    try:
                        neighbors[self.scenario.cell_to(cell, tuple(neighbor))] = self._load_vicinity(vicinity)
                    except OverflowError:
                        continue
        return neighbors


class GridCell(Cell[C, S, V], ABC, Generic[S, V]):
    def __init__(self, config: GridCellConfig):
        super().__init__(config)
        self.scenario = config.scenario

    @property
    def location(self) -> C:
        return self.cell_id

    def minkowski_distance(self, p: int, other: C) -> float:
        return self.scenario.minkowski_distance(p, self.location, other)

    def manhattan_distance(self, other: C) -> int:
        return self.scenario.minkowski_distance(1, self.location, other)

    def euclidean_distance(self, other: C) -> float:
        return self.scenario.minkowski_distance(2, self.location, other)

    def chebyshev_distance(self, other: C) -> int:
        return self.scenario.minkowski_distance(math.inf, self.location, other)

    def neighbor(self, relative: C) -> C:
        return self.scenario.cell_to(self.location, relative)

    def relative(self, neighbor: C) -> C:
        return self.scenario.distance_vector(self.location, neighbor)
