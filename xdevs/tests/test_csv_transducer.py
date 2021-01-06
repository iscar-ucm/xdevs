from unittest import TestCase

from xdevs.examples.devstone.devstone import LI, DelayedAtomic
from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor, Gpt


class TestCsvTransducer(TestCase):

    def check_component_filtering_by_type(self):
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

    def check_component_filtering_by_regex(self):
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

    def check_component_filtering_by_callable(self):
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



# Atomic_%d_%d
#
# if __name__ == '__main__':
#     csv = Transducers.create_transducer('csv', transducer_id='transducer_test')
#
#     model = Processor('processor', 100)
#     csv.add_target_component(model)
#     csv.add_target_port(model.o_out)
#
#     # model = Gpt("gpt", 3, 100)
#     # csv.add_target_component(model)
#     # csv.filter_components(Processor)
#
#     # Try to comment and uncomment the mapper lines to see the effect on the output file
#     # csv.state_mapper = {'current_job': (str, lambda x: str(x.current_job))}
#     csv.state_mapper['current_job'] = (str, lambda x: str(x.current_job))
#     csv.event_mapper = {'name': (int, lambda x: x.name), 'time': (int, lambda x: x.time)}
#
#     csv.initialize_transducer()
#     clock = 0
#     csv.activate_transducer(clock, [model], [])
#
#     model.i_in.add(Job(0))
#     model.deltext(1)
#     clock += 1
#     model.i_in.clear()
#     csv.activate_transducer(1, [model], [])
#     clock += model.sigma
#     model.lambdaf()
#     model.deltint()
#     csv.activate_transducer(clock, [model], [model.o_out])
#
#     print('done')
