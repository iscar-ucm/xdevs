import csv
import sys
import time

from devstone import LI, HI, HO
from xdevs.sim import Coordinator

sys.setrecursionlimit(10000)

sim_max_time = 1e10
int_delay = 0
ext_delay = 0
flatten = False
num_execs = int(sys.argv[1]) if len(sys.argv) > 1 else 10

depths_widths = [(300, 10), (10, 300), (300, 300)]

filename = "xdevs_devstone_%s_%dc_%di_%de_%d.csv" % ("flatten" if flatten else "noflatten", len(depths_widths),
                                                     int_delay, ext_delay, int(time.time()))

with open(filename, "w") as csv_file:
    csv_writer = csv.writer(csv_file, delimiter=';')
    csv_writer.writerow(("model", "depth", "width", "chained", "model_time", "runner_time", "sim_time", "total_time"))

    for depth, width in depths_widths:
        for model_type in (LI, HI, HO):
            class_name = model_type.__name__
            row = (class_name, depth, width)

            for chain_activated in (False, True):
                for i_exec in range(num_execs):

                    start = time.time()
                    root_model = model_type("root", depth, width, int_delay, ext_delay)
                    middle = time.time()
                    coord = Coordinator(root_model, flatten=flatten, chain=chain_activated)
                    coord.initialize()
                    coord.inject(root_model.i_in, 0)  # TODO How many input?
                    middle2 = time.time()
                    coord.simulate_time(sim_max_time)
                    end = time.time()

                    model_time = middle - start
                    runner_time = middle2 - middle
                    sim_time = end - middle2
                    total_time = end - start
                    # print("acc: %d" % i_exec, model_time, runner_time, sim_time, total_time)

                    # row = row + (model_time/num_execs, runner_time/num_execs, sim_time/num_execs, total_time/num_execs)
                    curr_row = row + (chain_activated, model_time, runner_time, sim_time, total_time)

                    print(curr_row)
                    csv_writer.writerow(curr_row)

