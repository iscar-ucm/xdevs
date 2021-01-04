from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor


if __name__ == '__main__':
    csv = Transducers.create_transducer('csv', transducer_id='transducer_test')

    model = Processor('processor', 100)
    csv.add_target_component(model)
    csv.add_target_port(model.o_out)

    # Try to comment and uncomment the mapper lines to see the effect on the output file
    # csv.state_mapper = {'current_job': (str, lambda x: str(x.current_job))}
    csv.state_mapper['current_job'] = (str, lambda x: str(x.current_job))
    csv.event_mapper = {'name': (int, lambda x: x.name), 'time': (int, lambda x: x.time)}

    csv.initialize_transducer()
    clock = 0
    csv.activate_transducer(clock, [model], [])

    model.i_in.add(Job(0))
    model.deltext(1)
    clock += 1
    model.i_in.clear()
    csv.activate_transducer(1, [model], [])
    clock += model.sigma
    model.lambdaf()
    model.deltint()
    csv.activate_transducer(clock, [model], [model.o_out])

    print('done')
