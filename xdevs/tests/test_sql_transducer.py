from xdevs.transducers import Transducers
from xdevs.examples.basic.basic import Job, Processor


if __name__ == '__main__':
    sql = Transducers.create_transducer('sql',
                                        transducer_id='transducer_test',
                                        sim_time_id='time',
                                        include_names=True,
                                        exhaustive=True,
                                        url='mysql+pymysql://root@localhost/test')

    model = Processor('processor', 100)
    sql.add_target_component(model)
    sql.add_target_port(model.o_out)

    # Try to comment and uncomment the mapper lines to see the effect on the output file
    # csv.state_mapper = {'current_job': (str, lambda x: str(x.current_job))}
    sql.state_mapper['current_job'] = (Job, lambda x: x.current_job)
    sql.event_mapper = {'name': (int, lambda x: x.name), 'time': (int, lambda x: x.time)}

    sql.initialize()
    clock = 0
    sql.bulk_data(clock)

    model.i_in.add(Job(0))
    model.deltext(1)
    clock += 1
    model.i_in.clear()
    sql.bulk_data(1)
    clock += model.sigma
    model.lambdaf()
    model.deltint()
    sql.bulk_data(clock)

    print('done')
