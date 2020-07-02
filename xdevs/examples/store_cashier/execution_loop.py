import subprocess
import re
import csv

CMD_XDEVS = "python3 store_cashier.py 00:30:00 {n_employees} 10 {t_new_clients} 0 0 {activate_chain}"
CMD_CADMIUM = "./STORE_CASHIER 00:30:00 {n_employees} 10 {t_new_clients} 0 0"

n_employees_range = range(10, 1001, 10)
t_new_clients_range = range(10, 0, -1)

with open("out.csv", "w") as csv_file:
    csv_writer = csv.writer(csv_file, delimiter=';')
    csv_writer.writerow(("engine", "n_employees", "t_new_clients", "chained", "model_time", "runner_time", "sim_time"))

    for engine_cmd in (CMD_CADMIUM, CMD_XDEVS):
        for chain_activated in (0, 1):
            if engine_cmd == CMD_CADMIUM and chain_activated == 1:
                continue

            for n_employees in range(10, 10011, 100):
                for t_new_clients in range(10, 0, -1):
                    exec_cmd = engine_cmd.format(n_employees=n_employees, t_new_clients=t_new_clients, activate_chain=chain_activated)
                    print("\n" + exec_cmd)
                    result = subprocess.run(exec_cmd.split(), stdout=subprocess.PIPE)
                    #print(result.stdout)

                    found = re.search("Elapsed time: ([0-9.e-]+) ?sec.*Elapsed time: ([0-9.e-]+) ?sec.*Simulation took: ([0-9.e-]+) ?sec", str(result.stdout))
                    if found:
                        print("Times: " + str(found.groups()))
                    else:
                        raise RuntimeError("Simulation times not found")

                    engine = "xdevs" if engine_cmd == CMD_XDEVS else "cadmium"
                    csv_writer.writerow((engine, n_employees, t_new_clients, chain_activated) + found.groups())
                csv_file.flush()
