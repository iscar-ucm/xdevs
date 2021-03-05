from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor


if __name__ == '__main__':
    es = Transducers.create_transducer('elasticsearch',
                                       transducer_id='transducer_test',
                                       exhaustive=True,
                                       url='http://localhost:9200')

    model = Processor('processor', 100)
    es.add_target_component(model)
    es.add_target_port(model.o_out)

    # Try to comment and uncomment the mapper lines to see the effect on the output file
    # csv.state_mapper = {'current_job': (str, lambda x: str(x.current_job))}
    es.state_mapper['current_job'] = (str, lambda x: str(x.current_job))
    es.event_mapper = {'name': (int, lambda x: x.name), 'time': (int, lambda x: x.time)}

    es.initialize()
    clock = 0
    es.bulk_data(clock)

    model.i_in.add(Job(0))
    model.deltext(1)
    clock += 1
    model.i_in.clear()
    es.bulk_data(1)
    clock += model.sigma
    model.lambdaf()
    model.deltint()
    es.bulk_data(clock)

    print('done')
