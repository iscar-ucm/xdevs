import os
from unittest import TestCase

from xdevs.models import Port, Coupled

from xdevs.sim import Coordinator

from xdevs.examples.devstone.devstone import LI, DelayedAtomic, HI
from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor, Gpt


class TestCsvTransducer(TestCase):

    def test_component_filtering_by_type(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)
        # csv.add_target_port()
        # csv.add_target_port_by_components(gpt, component_filter=[Coupled, "coupled_.*"], port_filter=OutPort)
        csv.filter_components(Processor)
        self.assertEqual(len(csv.target_components), 1)

        csv = Transducers.create_transducer('csv', transducer_id='tt')
        li = LI("LI_root", depth=10, width=10, int_delay=0, ext_delay=0)
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
        li = LI("LI_root", depth=10, width=10, int_delay=0, ext_delay=0)
        csv.add_target_component(li)
        csv.filter_components("Atomic_[1-3]_[157]")
        self.assertEqual(9, len(csv.target_components))

    def test_component_filtering_by_callable(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)
        csv.filter_components(lambda comp: hasattr(comp, "proc_time"))
        self.assertEqual(1, len(csv.target_components))

        csv = Transducers.create_transducer('csv', transducer_id='tt')
        li = LI("LI_root", depth=10, width=10, int_delay=0, ext_delay=0)
        csv.add_target_component(li)
        csv.filter_components(lambda comp: isinstance(comp, DelayedAtomic) and comp.name[-1] == "0")
        self.assertEqual(10, len(csv.target_components))

    def test_ports_filtering_by_type(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)

        csv.add_target_ports_by_component(gpt, port_filters=Port)
        self.assertEqual(8, len(csv.target_ports))

    def test_ports_filtering_by_regex(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)

        csv.add_target_ports_by_component(gpt, port_filters="i_.*")
        self.assertEqual(5, len(csv.target_ports))
        csv.target_ports.clear()

        csv.add_target_ports_by_component(gpt, port_filters="[io]_.{3,5}ed")
        self.assertEqual(2, len(csv.target_ports))
        csv.target_ports.clear()

        csv.add_target_ports_by_component(gpt, port_filters=".*start.*")
        self.assertEqual(1, len(csv.target_ports))
        csv.target_ports.clear()

    def test_ports_filtering_by_callable(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)

        filter_func = lambda port: port.name.startswith("i_") and port.name.endswith("ed")
        csv.add_target_ports_by_component(gpt, port_filters=filter_func)
        self.assertEqual(2, len(csv.target_ports))

    def test_ports_filtering_mixed(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        gpt = Gpt("gpt", 3, 100)
        csv.add_target_component(gpt)

        csv.add_target_ports_by_component(gpt)
        self.assertEqual(8, len(csv.target_ports))
        csv.target_ports.clear()

        input_ports_filter = lambda port: port.name.startswith("i_")
        port_filters = (input_ports_filter, Port, ".*ed")
        csv.add_target_ports_by_component(gpt, port_filters=port_filters)
        self.assertEqual(2, len(csv.target_ports))
        csv.target_ports.clear()

    def test_ports_filtering_mixed2(self):
        csv = Transducers.create_transducer('csv', transducer_id='tt')
        hi = HI("HI_root", depth=10, width=10, int_delay=0, ext_delay=0)

        comp_filters = (lambda comp: isinstance(comp, DelayedAtomic), ".*[0-2]$")
        csv.add_target_component(hi, *comp_filters)

        print(csv.target_components)
        self.assertEqual(28, len(csv.target_components))

        comp_filters2 = (DelayedAtomic, "Atomic_[35]_.*")
        csv.add_target_ports_by_component(hi, component_filters=comp_filters2, port_filters=("i_.*",))
        self.assertEqual(18, len(csv.target_ports))

        csv.add_target_ports_by_component(hi, component_filters=(Coupled,), port_filters=(Port, "o_.*"))
        self.assertEqual(18+10, len(csv.target_ports))

    def test_basic_behavior(self):
        trans_id = "%s_test_basic_behavior" % self.__class__.__name__
        csv = Transducers.create_transducer('csv', transducer_id=trans_id, exhaustive=True)

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
        csv_transducer = Transducers.create_transducer('csv', transducer_id=trans_id, exhaustive=True)

        gpt = Gpt("gpt", 3, 1000)
        csv_transducer.add_target_component(gpt)

        coord = Coordinator(gpt, flatten=False)
        coord.add_transducer(csv_transducer)
        coord.initialize()
        coord.simulate_time()
        coord.exit()

        self.assertTrue(os.path.exists(csv_transducer.state_filename))
        #self.assertTrue(os.path.exists(csv_transducer.event_filename))
        # TODO: continue when state changes are register appropiately

    # TODO: def test_pause_resume(self):
