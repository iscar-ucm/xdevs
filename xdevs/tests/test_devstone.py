from unittest import TestCase

from xdevs.sim import Coordinator, ParallelCoordinator
from xdevs.examples.devstone.devstone import LI, HI, HO, HOmod, DelayedAtomic
from xdevs.models import Atomic, Coupled

import random


class Utils:

    @staticmethod
    def count_atomics(coupled):
        """
        :return: Number of atomic components in a coupled model
        """
        atomic_count = 0
        for comp in coupled.components:
            if isinstance(comp, Atomic):
                atomic_count += 1
            elif isinstance(comp, Coupled):
                atomic_count += Utils.count_atomics(comp)
            else:
                raise RuntimeError("Unrecognized type of component")

        return atomic_count

    @staticmethod
    def count_ic(coupled):
        """
        :return: Number of ic couplings in a coupled model
        """
        ic_count = sum([len(cl) for cl in coupled.ic.values()])
        for comp in coupled.components:
            if isinstance(comp, Coupled):
                ic_count += Utils.count_ic(comp)
            elif not isinstance(comp, Atomic):
                raise RuntimeError("Unrecognized type of component")

        return ic_count

    @staticmethod
    def count_eic(coupled):
        """
        :return: Number of eic couplings in a coupled model
        """
        eic_count = sum([len(cl) for cl in coupled.eic.values()])
        for comp in coupled.components:
            if isinstance(comp, Coupled):
                eic_count += Utils.count_eic(comp)
            elif not isinstance(comp, Atomic):
                raise RuntimeError("Unrecognized type of component")

        return eic_count

    @staticmethod
    def count_eoc(coupled):
        """
        :return: Number of eoc couplings in a coupled model
        """
        eoc_count = sum([len(cl) for cl in coupled.eoc.values()])
        for comp in coupled.components:
            if isinstance(comp, Coupled):
                eoc_count += Utils.count_eoc(comp)
            elif not isinstance(comp, Atomic):
                raise RuntimeError("Unrecognized type of component")

        return eoc_count

    @staticmethod
    def count_transitions(coupled):
        """
        :return: Number of atomic components in a coupled model
        """
        int_count = 0
        ext_count = 0
        for comp in coupled.components:
            if isinstance(comp, DelayedAtomic):
                int_count += comp.int_count
                ext_count += comp.ext_count
            elif isinstance(comp, Coupled):
                pic, pec = Utils.count_transitions(comp)
                int_count += pic
                ext_count += pec
            else:
                raise RuntimeError("Unrecognized type of component")

        return int_count, ext_count


class DevstoneUtilsTestCase(TestCase):

    def __init__(self, name, num_valid_params_sets: int = 10):
        super().__init__(name)
        self.valid_high_params = []
        self.valid_low_params = []

        for _ in range(int(num_valid_params_sets)):
            self.valid_high_params.append([random.randint(1, 100), random.randint(1, 200),
                                           0, 0])

        for _ in range(int(num_valid_params_sets)):
            self.valid_low_params.append([random.randint(1, 20), random.randint(1, 30),
                                      0, 0])

    def check_invalid_inputs(self, base_class):
        self.assertRaises(ValueError, base_class, "root", 0, 1, 1, 1)
        self.assertRaises(ValueError, base_class, "root", 1, 0, 1, 1)
        self.assertRaises(ValueError, base_class, "root", 1, 1, -1, 0)
        self.assertRaises(ValueError, base_class, "root", 1, 1, 0, -1)
        self.assertRaises(ValueError, base_class, "root", 0, 1, -1, -1)
        self.assertRaises(ValueError, base_class, "root", 0, 0, -1, -1)


class TestLI(DevstoneUtilsTestCase):

    def test_structure(self):
        """
        Check structure params: atomic modules, ic's, eic's and eoc's.
        """
        for params_tuple in self.valid_high_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                self._check_structure(**params)

    def test_structure_corner_cases(self):
        params = {"depth": 10, "width": 1, "int_delay": 1, "ext_delay": 1}
        self._check_structure(**params)
        params["depth"] = 1
        self._check_structure(**params)

    def _check_structure(self, **params):
        li_root = LI("LI_root", **params)
        self.assertEqual(Utils.count_atomics(li_root), (params["width"] - 1) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eic(li_root), params["width"] * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eoc(li_root), params["depth"])
        self.assertEqual(Utils.count_ic(li_root), 0)

    def _test_behavior(self, coord_type):
        """
        Check behaviour params: number of int and ext transitions.
        """
        for params_tuple in self.valid_low_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                li_root = LI("LI_root", **params)
                coord = coord_type(li_root, flatten=False)
                coord.initialize()
                coord.inject(li_root.i_in, 0)
                coord.simulate()

                int_count, ext_count = Utils.count_transitions(li_root)
                self.assertEqual(int_count, (params["width"] - 1) * (params["depth"] - 1) + 1)
                self.assertEqual(ext_count, (params["width"] - 1) * (params["depth"] - 1) + 1)

    def test_behavior_sequential(self):
        self._test_behavior(Coordinator)

    def test_behavior_parallel(self):
        self._test_behavior(ParallelCoordinator)

    def test_invalid_inputs(self):
        super().check_invalid_inputs(LI)


class TestHI(DevstoneUtilsTestCase):

    def test_structure(self):
        """
        Check structure params: atomic modules, ic's, eic's and eoc's.
        """
        for params_tuple in self.valid_high_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                self._check_structure(**params)

    def test_structure_corner_cases(self):
        params = {"depth": 10, "width": 1, "int_delay": 1, "ext_delay": 1}
        self._check_structure(**params)
        params["depth"] = 1
        self._check_structure(**params)

    def _check_structure(self, **params):
        hi_root = HI("HI_root", **params)
        self.assertEqual(Utils.count_atomics(hi_root), (params["width"] - 1) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eic(hi_root), params["width"] * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eoc(hi_root), params["depth"])
        self.assertEqual(Utils.count_ic(hi_root),
                         (params["width"] - 2) * (params["depth"] - 1) if params["width"] > 2 else 0)

    def _test_behavior(self, coord_type):
        """
        Check behaviour params: number of int and ext transitions.
        """
        for params_tuple in self.valid_low_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                hi_root = HI("HI_root", **params)
                coord = coord_type(hi_root, flatten=False)
                coord.initialize()
                coord.inject(hi_root.i_in, 0)
                coord.simulate()

                int_count, ext_count = Utils.count_transitions(hi_root)
                self.assertEqual(int_count, (((params["width"] - 1) * params["width"]) / 2) * (params["depth"] - 1) + 1)
                self.assertEqual(ext_count, (((params["width"] - 1) * params["width"]) / 2) * (params["depth"] - 1) + 1)

    def test_behavior_sequential(self):
        self._test_behavior(Coordinator)

    def test_behavior_parallel(self):
        self._test_behavior(ParallelCoordinator)

    def test_invalid_inputs(self):
        super().check_invalid_inputs(HI)


class TestHO(DevstoneUtilsTestCase):

    def test_structure(self):
        """
        Check structure params: atomic modules, ic's, eic's and eoc's.
        """
        for params_tuple in self.valid_high_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                self._check_structure(**params)

    def test_structure_corner_cases(self):
        params = {"depth": 10, "width": 1, "int_delay": 1, "ext_delay": 1}
        self._check_structure(**params)
        params["depth"] = 1
        self._check_structure(**params)

    def _check_structure(self, **params):
        ho_root = HO("HO_root", **params)
        self.assertEqual(Utils.count_atomics(ho_root), (params["width"] - 1) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eic(ho_root), (params["width"] + 1) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eoc(ho_root), params["width"] * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_ic(ho_root),
                         (params["width"] - 2) * (params["depth"] - 1) if params["width"] > 2 else 0)

    def _test_behavior(self, coord_type):
        """
        Check behaviour params: number of int and ext transitions.
        """
        for params_tuple in self.valid_low_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                ho_root = HO("HO_root", **params)
                coord = coord_type(ho_root, flatten=False)
                coord.initialize()
                coord.inject(ho_root.i_in, 0)
                coord.inject(ho_root.i_in2, 0)
                coord.simulate()

                int_count, ext_count = Utils.count_transitions(ho_root)
                self.assertEqual(int_count, (((params["width"] - 1) * params["width"]) / 2) * (params["depth"] - 1) + 1)
                self.assertEqual(ext_count, (((params["width"] - 1) * params["width"]) / 2) * (params["depth"] - 1) + 1)

    def test_behavior_sequential(self):
        self._test_behavior(Coordinator)

    def test_behavior_parallel(self):
        self._test_behavior(ParallelCoordinator)

    def test_invalid_inputs(self):
        super().check_invalid_inputs(HO)


class TestHOmod(DevstoneUtilsTestCase):

    def test_structure(self):
        """
        Check structure params: atomic modules, ic's, eic's and eoc's.
        """
        for params_tuple in self.valid_low_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                self._check_structure(**params)

    def test_structure_corner_cases(self):
        params = {"depth": 10, "width": 1, "int_delay": 1, "ext_delay": 1}
        self._check_structure(**params)
        params["depth"] = 1
        self._check_structure(**params)

    def _check_structure(self, **params):
        ho_mod_root = HOmod("HOmod_root", **params)
        self.assertEqual(Utils.count_atomics(ho_mod_root), ((params["width"] - 1) + ((params["width"] - 1) * params["width"]) / 2) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eic(ho_mod_root), (2*(params["width"] - 1) + 1) * (params["depth"] - 1) + 1)
        self.assertEqual(Utils.count_eoc(ho_mod_root), params["depth"])
        # ICs relative to the "triangular" section
        exp_ic = (((params["width"] - 2) * (params["width"] - 1)) / 2) if params["width"] > 1 else 0
        # Plus the ones relative to the connection from the 2nd to 1st row...
        exp_ic += (params["width"] - 1) ** 2
        # ...and from the 1st to the couple component
        exp_ic += params["width"] - 1
        # Multiplied by the number of layers (except the deepest one, that doesn't have ICs)
        exp_ic *= (params["depth"] - 1)
        self.assertEqual(Utils.count_ic(ho_mod_root), exp_ic)

    def _test_behavior(self, coord_type):
        """
        Check behaviour params: number of int and ext transitions.
        """
        for params_tuple in self.valid_low_params:
            params = dict(zip(("depth", "width", "int_delay", "ext_delay"), params_tuple))

            with self.subTest(**params):
                ho_mod_root = HOmod("HOmod_root", **params)
                coord = coord_type(ho_mod_root, flatten=False)
                coord.initialize()
                coord.inject(ho_mod_root.i_in, 0)
                coord.inject(ho_mod_root.i_in2, 0)
                coord.simulate()

                calc_in = lambda x, w: 1 + (x - 1)*(w - 1)
                exp_trans = 1
                tr_atomics = sum(range(1, params["width"]))
                for i in range(1, params["depth"]):
                    num_inputs = calc_in(i, params["width"])
                    trans_first_row = (params["width"] - 1) * (num_inputs + params["width"] - 1)
                    exp_trans += num_inputs * tr_atomics + trans_first_row
                int_count, ext_count = Utils.count_transitions(ho_mod_root)
                self.assertEqual(int_count, exp_trans)
                self.assertEqual(ext_count, exp_trans)

    def test_invalid_inputs(self):
        super().check_invalid_inputs(HOmod)

    def test_behavior_sequential(self):
        self._test_behavior(Coordinator)
