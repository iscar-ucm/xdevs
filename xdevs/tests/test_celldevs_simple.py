import json
import unittest
from copy import deepcopy
from typing import Dict
from xdevs.celldevs.cell import CellConfig


class DummyState:
    def __init__(self, **kwargs):
        self.start: int = kwargs['start']
        self.end: int = kwargs.get('end', self.start * 2)
        self.log: bool = kwargs.get('log', False)


class TestCellDEVSSimple(unittest.TestCase):
    def test_config(self):
        configs: Dict[str, CellConfig[str, DummyState, float]] = dict()
        with open('celldevs_simple.json') as file:
            raw_configs = json.load(file)['cells']
        default_config = CellConfig('default', str, DummyState, float, **raw_configs.pop('default'))
        configs['default'] = default_config
        for config_id, params in raw_configs.items():
            config = deepcopy(default_config)
            config.apply_patch(config_id, **params)
            configs[config_id] = config

        # First, we test the default configuration
        config = configs['default']
        self.assertEqual(config.config_id, 'default')
        self.assertEqual(config.c_type, str)
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
        neighborhood = config.load_neighborhood()
        self.assertFalse(neighborhood)
        self.assertFalse(config.eic)
        self.assertEqual(len(config.ic), 1)
        self.assertTrue(('out_celldevs', 'in_celldevs') in config.ic)
        self.assertFalse(config.eoc)

        # Second, we test the simple configuration
        config = configs['simple']
        self.assertEqual(config.config_id, 'simple')
        self.assertEqual(config.c_type, str)
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
        neighborhood = config.load_neighborhood()
        self.assertEqual(len(neighborhood), 2)
        self.assertEqual(neighborhood['simple'], 1.0)
        self.assertEqual(neighborhood['complex'], 0.0)
        self.assertFalse(config.eic)
        self.assertEqual(len(config.ic), 1)
        self.assertTrue(('out_celldevs', 'in_celldevs') in config.ic)
        self.assertFalse(config.eoc)

        # Third, we test the complex configuration
        config = configs['complex']
        self.assertEqual(config.config_id, 'complex')
        self.assertEqual(config.c_type, str)
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
        neighborhood = config.load_neighborhood()
        self.assertEqual(len(neighborhood), 2)
        self.assertEqual(neighborhood['simple'], 0.0)
        self.assertEqual(neighborhood['complex'], 1.0)
        self.assertEqual(len(config.eic), 1)
        self.assertTrue(('in_start', 'in_start') in config.eic)
        self.assertEqual(len(config.ic), 1)
        self.assertTrue(('out_celldevs', 'in_celldevs') in config.ic)
        self.assertEqual(len(config.eoc), 2)
        self.assertTrue(('out_stats', 'out_stats') in config.eoc)
        self.assertTrue(('out_done', 'out_done') in config.eoc)


if __name__ == '__main__':
    unittest.main()
