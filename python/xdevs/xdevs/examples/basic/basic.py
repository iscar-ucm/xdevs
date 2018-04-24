
from xdevs import PHASE_ACTIVE, PHASE_PASSIVE, INFINITY
from xdevs.models import Atomic, Coupled, Port
from xdevs.sim import Coordinator

PHASE_DONE = "done"

class Job:
	
	def __init__(self):
		self.name = name
		self.time = 0
		
		
class Generator(Atomic):

	def __init__(self, name, period):
		super().__init__(name)
		
		self.i_start = Port(Job, "i_start")
		self.i_stop = Port(Job, "i_stop")
		self.o_out = Port(Job, "o_out")
		
		self.add_in_port(self.i_start)
		self.add_in_port(self.i_stop)
		self.add_out_port(self.o_out)
		
		self.period = period
		self.job_counter = 1
		
	def initialize(self):
		self.hold_in(PHASE_ACTIVE, self.period)
		
	def exit(self):
		pass
		
	def deltint(self):
		self.job_counter += 1
		self.hold_in(PHASE_ACTIVE, period)
	
	def deltext(self, e):
		self.passivate()
	
	def lambdaf(self):
		self.o_out.add(Job(str(job_counter)))
		
		
class Processor(Atomic):

	

	def __init__(self, name, proc_time):
		super().__init__(name)
		
		self.i_in = Port(Job, "i_in")
		self.o_out = Port(Job, "o_out")
		
		self.add_in_port(self.i_in)
		self.add_out_port(self.o_out)

		self.current_job = None
		self.proc_time = proc_time
		
	def initialize(self):
		self.passivate()
		
	def exit(self):
		pass
		
	def deltint(self):
		self.passivate()
	
	def deltext(self, e):
		if self.phase == PHASE_PASSIVE:
			self.current_job = i_in.get()
			self.hold_in(PHASE_ACTIVE, self.proc_time)
	
	def lambdaf(self):
		self.o_out.add(current_job)

		
class Transducer(Atomic):

	def __init__(self, name, obs_time):
		super().__init__(name)
		
		self.i_arrived = Port(Job, "i_arrived")
		self.i_solved = Port(Job, "i_solved")
		self.o_out = Port(Job, "o_out")
		
		self.add_in_port(self.i_start)
		self.add_in_port(self.i_stop)
		self.add_out_port(self.o_out)
		
		self.jobs_arrived = []
		self.jobs_solved = []
		
		self.total_ta = 0
		self.clock = 0
		self.obs_time = obs_time
		
	def initialize(self):
		self.hold_in(PHASE_ACTIVE, self.obs_time)
		
	def exit(self):
		pass
		
	def deltint(self):
		self.clock += self.sigma
		throughput = 0
		avg_ta = 0
		
		if self.phase == PHASE_ACTIVE:
			if self.jobs_solved:
				avg_ta = self.total_ta / len(self.jobs_solved)
				throughput = len(self.jobs_solved) / self.clock if self.clock > 0 else 0
			else:
				avg_ta = 0
				throughput = 0
				
			logging.info("End time: %d" % self.clock)
			logging.info("Jobs arrived: %d" % len(self.jobs_arrived))
			logging.info("Jobs solved: %d" % len(self.jobs_solved))
			logging.info("Average TA: %d" % avg_ta)
			logging.info("Throughput: %d" % throughput)
			
			holdIn(PHASE_DONE, 0);
		else:
			self.passivate()
			
	def deltext(self, e):
		self.clock += e
		
		if self.phase == PHASE_ACTIVE:
			job = None
			
			if i_arrived:
				job = i_arrived.get()
				logging.info("Starting job %s @ t = %d" % (job.name, clock))
				job.time = self.clock
				self.jobs_arrived.append(job)
				
			if i_solved:
				job = i_solved.get()
				logging.info("Job %s finished @ t = %d" % (job.name, clock))
				job.time = self.clock
				self.jobs_solved.append(job)
	
	def lambdaf(self):
		if self.phase == PHASE_DONE:
			self.o_out.add(Job("null"))
		

class Gpt(Coupled):

	def __init__(self, name, period, obs_time):
		super().__init__(name)
		
		gen = Generator("generator", period)
		proc = Processor("processor", 3*period)
		trans = Transducer("transducer", obs_time)
		
		self.add_component(gen)
		self.add_component(proc)
		self.add_component(trans)
		
		self.add_coupling(gen, gen.o_out, proc, proc.i_in)
		self.add_coupling(gen, gen.o_out, trans, trans.i_arrived)
		self.add_coupling(proc, proc.o_out, trans, trans.i_solved)
		self.add_coupling(trans, trans.o_out, gen, gen.i_stop)
		
	
gpt = Gpt("gpt", 1, 100)
coord = Coordinator(gpt)
coord.initialize()
coord.simulate(INFINITY)
