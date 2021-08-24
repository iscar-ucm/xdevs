import json
import math
import unittest
from copy import deepcopy
from typing import Dict
from xdevs.celldevs.grid import GridScenario, GridCellConfig


class DummyState:
    def __init__(self, **kwargs):
        self.start: int = kwargs['start']
        self.end: int = kwargs.get('end', self.start * 2)
        self.log: bool = kwargs.get('log', False)


class MyTestCase(unittest.TestCase):
    def test_invalid_scenarios(self):
        try:
            GridScenario(tuple())
            self.assertEqual(True, False)
        except ValueError:
            pass
        try:
            GridScenario((1, 2, 0))
            self.assertEqual(True, False)
        except ValueError:
            pass
        try:
            GridScenario((3, 3, 3), origin=(1, 2))
            self.assertEqual(True, False)
        except ValueError:
            pass
        try:
            GridScenario((3, 3, 3), origin=(1, 2, 3, 4))
            self.assertEqual(True, False)
        except ValueError:
            pass

    def test_moore_neighborhood(self):
        for d in range(1, 5):
            center = tuple(0 for _ in range(d))
            for r in range(4):
                shape = tuple(2 * r + 1 for _ in range(d))
                origin = tuple(-r for _ in range(d))
                neighbors = GridScenario._moore_neighborhood(d, r)
                self.assertEqual(len(neighbors), (2 * r + 1) ** d)
                for neighbor in neighbors:
                    distance = GridScenario._minkowski_distance(math.inf, center, neighbor, shape, origin, False)
                    self.assertLessEqual(distance, r)

    def test_von_neumann_neighborhood(self):
        for d in range(1, 5):
            center = tuple(0 for _ in range(d))
            for r in range(4):
                shape = tuple(2 * r + 1 for _ in range(d))
                origin = tuple(-r for _ in range(d))
                neighbors = GridScenario._von_neumann_neighborhood(d, r)
                for neighbor in neighbors:
                    distance = GridScenario._minkowski_distance(1, center, neighbor, shape, origin, False)
                    self.assertLessEqual(distance, r)

    def test_unwrapped_scenario(self):
        scenario = GridScenario((5, 5, 5), origin=(-2, -2, -2))

        self.assertTrue(scenario.cell_in_scenario((-2, -2, -2)))
        self.assertFalse(scenario.cell_in_scenario((-2, -2, -3)))
        self.assertTrue(scenario.cell_in_scenario((2, 2, 2)))
        self.assertFalse(scenario.cell_in_scenario((2, 2, 3)))

        self.assertEqual(scenario.distance_vector((-2, -2, -2), (2, 2, 2)), (4, 4, 4))
        self.assertEqual(scenario.distance_vector((0, -1, -2), (0, 1, 2)), (0, 2, 4))

        self.assertEqual(scenario.cell_to((-2, -2, -2), (4, 4, 4)), (2, 2, 2))
        self.assertEqual(scenario.cell_to((0, -1, -2), (0, 2, 4)), (0, 1, 2))

        self.assertEqual(scenario.minkowski_distance(1, (-2, -2, -2), (2, 2, 2)), 12)
        self.assertEqual(scenario.minkowski_distance(2, (-2, -2, -2), (2, 2, 2)), (16 * 3) ** (1 / 2))
        self.assertEqual(scenario.minkowski_distance(math.inf, (-2, -2, -2), (2, 2, 2)), 4)

        self.assertEqual(scenario.minkowski_distance(1, (0, -1, -2), (0, 1, 2)), 6)
        self.assertEqual(scenario.minkowski_distance(2, (0, -1, -2), (0, 1, 2)), (4 + 16) ** (1 / 2))
        self.assertEqual(scenario.minkowski_distance(math.inf, (0, -1, -2), (0, 1, 2)), 4)

    def test_wrapped_scenario(self):
        scenario = GridScenario((5, 5, 5), origin=(-2, -2, -2), wrapped=True)

        self.assertTrue(scenario.cell_in_scenario((-2, -2, -2)))
        self.assertFalse(scenario.cell_in_scenario((-2, -2, -3)))
        self.assertTrue(scenario.cell_in_scenario((2, 2, 2)))
        self.assertFalse(scenario.cell_in_scenario((2, 2, 3)))

        self.assertEqual(scenario.distance_vector((-2, -2, -2), (2, 2, 2)), (-1, -1, -1))
        self.assertEqual(scenario.distance_vector((0, -1, -2), (0, 1, 2)), (0, 2, -1))

        self.assertEqual(scenario.cell_to((-2, -2, -2), (4, 4, 4)), (2, 2, 2))
        self.assertEqual(scenario.cell_to((-2, -2, -2), (-1, -1, -1)), (2, 2, 2))
        self.assertEqual(scenario.cell_to((0, -1, -2), (0, 2, 4)), (0, 1, 2))
        self.assertEqual(scenario.cell_to((0, -1, -2), (0, 2, -1)), (0, 1, 2))

        self.assertEqual(scenario.minkowski_distance(1, (-2, -2, -2), (2, 2, 2)), 3)
        self.assertEqual(scenario.minkowski_distance(2, (-2, -2, -2), (2, 2, 2)), 3 ** (1 / 2))
        self.assertEqual(scenario.minkowski_distance(math.inf, (-2, -2, -2), (2, 2, 2)), 1)

        self.assertEqual(scenario.minkowski_distance(1, (0, -1, -2), (0, 1, 2)), 3)
        self.assertEqual(scenario.minkowski_distance(2, (0, -1, -2), (0, 1, 2)), 5 ** (1 / 2))
        self.assertEqual(scenario.minkowski_distance(math.inf, (0, -1, -2), (0, 1, 2)), 2)

    def test_config(self):
        configs: Dict[str, GridCellConfig[DummyState, float]] = dict()
        with open('celldevs_grid.json') as file:
            raw = json.load(file)
            scenario = GridScenario(**raw['scenario'])
            raw_configs = raw['cells']
        default_config = GridCellConfig(scenario, 'default', DummyState, float, **raw_configs.pop('default'))
        configs['default'] = default_config
        for config_id, params in raw_configs.items():
            config = deepcopy(default_config)
            config.apply_patch(config_id, **params)
            configs[config_id] = config

        # First, we test the default configuration
        config = configs['default']
        self.assertEqual(config.config_id, 'default')
        self.assertEqual(config.c_type, tuple)
        self.assertEqual(config.s_type, DummyState)
        self.assertEqual(config.v_type, float)
        self.assertEqual(config.cell_type, 'simple')
        self.assertEqual(config.delay_type, 'inertial')
        self.assertIsNone(config.cell_map)
        self.assertEqual(config.cell_config, [1, 2, 3])
        state = config.load_state()
        self.assertIsInstance(state, DummyState)
        self.assertEqual(state.start, 1)
        self.assertEqual(state.end, 2)
        self.assertFalse(state.log)
        neighborhood = config.load_cell_neighborhood((0, 0))
        for neighbor, vicinity in neighborhood.items():
            self.assertLessEqual(scenario.minkowski_distance(1, (0, 0), neighbor), 2)
            if neighbor == (0, 0):
                self.assertEqual(vicinity, 1)
            else:
                self.assertEqual(vicinity, 0.5)
        self.assertFalse(config.eic)
        self.assertEqual(len(config.ic), 1)
        self.assertTrue(('out_celldevs', 'in_celldevs') in config.ic)
        self.assertFalse(config.eoc)

        # Then, we test the complex configuration
        config = configs['complex']
        self.assertEqual(config.config_id, 'complex')
        self.assertEqual(config.c_type, tuple)
        self.assertEqual(config.s_type, DummyState)
        self.assertEqual(config.v_type, float)
        self.assertEqual(config.cell_type, 'complex')
        self.assertEqual(config.delay_type, 'transport')
        self.assertIsNone(config.cell_map)
        self.assertEqual(config.cell_config, [4, 5, 6])
        state = config.load_state()
        self.assertIsInstance(state, DummyState)
        self.assertEqual(state.start, 1)
        self.assertEqual(state.end, 5)
        self.assertTrue(state.log)
        neighborhood = config.load_cell_neighborhood((0, 0))
        for neighbor, vicinity in neighborhood.items():
            self.assertLessEqual(scenario.minkowski_distance(1, (0, 0), neighbor), 2)
            if neighbor == (0, 0):
                self.assertEqual(vicinity, 1)
            else:
                self.assertEqual(vicinity, 0.5)
        self.assertEqual(len(config.eic), 1)
        self.assertTrue(('in_start', 'in_start') in config.eic)
        self.assertEqual(len(config.ic), 1)
        self.assertTrue(('out_celldevs', 'in_celldevs') in config.ic)
        self.assertEqual(len(config.eoc), 2)
        self.assertTrue(('out_stats', 'out_stats') in config.eoc)
        self.assertTrue(('out_done', 'out_done') in config.eoc)


if __name__ == '__main__':
    unittest.main()
