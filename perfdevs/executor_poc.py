
from concurrent import futures
from perfdevs.examples.devstone.pystone import pystones

def task():
    print("Starting...")
    pow(88888, 888888)
    print("Finishing...")

def task2():
    print("Starting...")
    pystones(1000000)
    print("Finishing...")


executor = futures.ProcessPoolExecutor(max_workers=8)

ex_futures = []
for _ in range(100):
    ex_futures.append(executor.submit(task2))

for _ in futures.as_completed(ex_futures):
    pass

