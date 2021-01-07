import os
import sys
from unittest import TestCase

from xdevs.sim import Coordinator

from xdevs.examples.devstone.devstone import LI, DelayedAtomic
from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor, Gpt


class TestCsvTransducer(TestCase):

    def test_component_filtering_by_type(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)
        csv.filter_components(Processor)
        self.assertEqual(len(csv.target_components), 1)

        csv = Transducers.create_transducer('csv', transducer_id='tt')
        li = LI("HI_root", depth=10, width=10, int_delay=0, ext_delay=0)
        csv.add_target_component(li)
        csv.filter_components(DelayedAtomic)
        self.assertEqual(82, len(csv.target_components))

    def test_component_filtering_by_regex(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)
        csv.filter_components(".*or")
        self.assertEqual(len(csv.target_components), 2)

        csv = Transducers.create_transducer('csv', transducer_id='tt')
        li = LI("HI_root", depth=10, width=10, int_delay=0, ext_delay=0)
        csv.add_target_component(li)
        csv.filter_components("Atomic_[1-3]_[157]")
        self.assertEqual(9, len(csv.target_components))

    def test_component_filtering_by_callable(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)
        csv.filter_components(lambda comp: hasattr(comp, "proc_time"))
        self.assertEqual(len(csv.target_components), 1)

        csv = Transducers.create_transducer('csv', transducer_id='tt')
        li = LI("HI_root", depth=10, width=10, int_delay=0, ext_delay=0)
        csv.add_target_component(li)
        csv.filter_components(lambda comp: isinstance(comp, DelayedAtomic) and comp.name[-1] == "0")
        self.assertEqual(10, len(csv.target_components))

    def test_basic_behavior(self):
        trans_id = "%s_test_basic_behavior" % self.__class__.__name__
        csv = Transducers.create_transducer('csv', transducer_id=trans_id)

        model = Processor('processor', 100)
        csv.add_target_component(model)
        csv.add_target_port(model.o_out)

        csv.state_mapper['current_job'] = (str, lambda x: str(x.current_job))
        csv.event_mapper = {'name': (int, lambda x: x.name), 'time': (int, lambda x: x.time)}

        csv.initialize()
        self.assertTrue(os.path.exists(csv.state_filename))
        self.assertTrue(os.path.exists(csv.event_filename))

        clock = 0
        csv.bulk_data(clock)

        model.i_in.add(Job(0))
        model.deltext(1)
        clock += 1
        model.i_in.clear()
        csv.bulk_data(clock)

        clock += model.sigma
        model.lambdaf()
        model.deltint()
        csv.bulk_data(clock)

        csv.exit()

        count_lines = 0
        with open(csv.state_filename, "r") as states_file:
            for line in states_file.readlines():
                if line and count_lines > 0:
                    fields = line.split(csv.delimiter)
                    self.assertEqual(model.name, fields[1])
                count_lines += 1
        self.assertEqual(4, count_lines)

        count_lines = 0
        with open(csv.event_filename, "r") as events_file:
            for line in events_file.readlines():
                if line and count_lines > 0:
                    fields = line.split(csv.delimiter)
                    self.assertEqual(model.name, fields[1])
                    self.assertEqual(model.o_out.name, fields[2])
                count_lines += 1
        self.assertEqual(2, count_lines)

    def test_behavior(self):
        trans_id = "%s_test_behavior" % self.__class__.__name__
        csv_transducer = Transducers.create_transducer('csv', transducer_id=trans_id)

        gpt = Gpt("gpt", 3, 1000)
        csv_transducer.add_target_component(gpt)

        coord = Coordinator(gpt, flatten=False, chain=False)
        coord.add_transducer(csv_transducer)
        coord.initialize()
        coord.simulate_time()
        coord.exit()

        self.assertTrue(os.path.exists(csv_transducer.state_filename))
        #self.assertTrue(os.path.exists(csv_transducer.event_filename))
