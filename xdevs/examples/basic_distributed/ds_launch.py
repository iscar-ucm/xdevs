
from xdevs.sim import DistributedSimulator

ds = DistributedSimulator("gpt.xml", "processor")
ds.listen()

# {"cmd": "initialize"}
# {"cmd": "inject", "port": "o_out", "values": [["basic.Job", {"name": 1}], ["basic.Job", {"name": 2}]]}
# {"cmd": "propagate_output"}
# {"cmd": "exit"}
