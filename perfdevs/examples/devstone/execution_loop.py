import csv, time
from perfdevs.examples.devstone.devstone import LI, HI, HO
from perfdevs.sim import Coordinator

sim_max_time = 1e10
int_delay = 10
ext_delay = 10
flatten = False

depths_widths = [(100, 5), (5, 100), (100, 100)]

filename = "xdevs_devstone_%s_%dc_%di_%de_%d.csv" % ("flatten" if flatten else "noflatten", len(depths_widths), int_delay, ext_delay, int(time.time()))

with open(filename, "w") as csv_file:
    csv_writer = csv.writer(csv_file, delimiter=';')
    csv_writer.writerow(("model", "depth", "width", "uch_model_time", "uch_runner_time", "uch_sim_time", "uch_total_time", "ch_model_time", "ch_runner_time", "ch_sim_time", "ch_total_time"))

    for depth, width in depths_widths:
        for model_type in (LI, HI, HO):
            class_name = model_type.__name__
            row = (class_name, depth, width)

            for chain_activated in (False, True):
                start = time.time()
                root_model = model_type("root", depth, width, int_delay, ext_delay)
                middle = time.time()
                coord = Coordinator(root_model, flatten=flatten, force_chain=chain_activated)
                coord.initialize()
                coord.inject(root_model.i_in, 0)  # TODO How many input?
                middle2 = time.time()
                coord.simulate_time(sim_max_time)
                end = time.time()

                model_time = middle - start
                runner_time = middle2 - middle
                sim_time = end - middle2
                total_time = end - start

                row = row + (model_time, runner_time, sim_time, total_time)

            print(row)
            csv_writer.writerow(row)



"""with open(filename, "w") as csv_file:
    csv_writer = csv.writer(csv_file, delimiter=';')
    csv_writer.writerow(("chained", "model", "depth", "width", "model_time", "runner_time", "sim_time", "total_time"))

    for depth, width in depths_widths:
        for chain_activated in (False, True):
            for model_type in (LI, HI, HO):
                start = time.time()
                root_model = model_type("root", depth, width, int_delay, ext_delay)
                middle = time.time()
                coord = Coordinator(root_model, flatten=flatten, force_chain=chain_activated)
                coord.initialize()
                coord.inject(root_model.i_in, 0)  # TODO How many input?
                middle2 = time.time()
                coord.simulate_time(sim_max_time)
                end = time.time()

                model_time = middle - start
                runner_time = middle2 - middle
                sim_time = end - middle2
                total_time = end - start

                class_name = root_model.__class__.__name__
                row = (int(chain_activated), class_name, depth, width, model_time, runner_time, sim_time, total_time)
                print(row)
                csv_writer.writerow(row)"""