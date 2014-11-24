package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Atomic
import java.util.logging.Logger
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort
import java.util.LinkedList

class Transducer extends Atomic{
	
	val static Logger logger = Logger.getLogger(Transducer.name)
	val protected InPort<Job> iArrived = new InPort<Job> ("iArrived")
	val protected InPort<Job> iSolved = new InPort<Job>("iSolved")
	val protected OutPort<Job> oOut = new OutPort<Job>("oOut")
	
	val LinkedList<Job> jobsArrived = new LinkedList<Job>()
	val LinkedList<Job> jobsSolved = new LinkedList<Job>()
	
	var double totalTa
	var double clock
	
	new (String name, double observationTime){
		super(name)
		totalTa = 0
		clock = 0
		addInPort(iArrived)
		addInPort(iSolved)
		addOutPort(oOut)
		holdIn("active", observationTime)
	}
	override deltext(double e) {
		clock = clock + e
		if(phaseIs("active")){
			var Job job = null
			if (!iArrived.isEmpty()) {
				job = iArrived.getSingleValue()
				logger.fine("Start job " + job.id + " @ t = " + clock)
				job.time = clock
				jobsArrived.add(job)
			}
			if (!iSolved.isEmpty()) {
				job = iSolved.getSingleValue()
				totalTa += (clock - job.time)
				logger.fine("Finish job " + job.id + " @ t = " + clock)
				job.time = clock
				jobsSolved.add(job)
			}
		}
	}
	
	override deltint() {
		clock = clock + getSigma()
		var double throughput
		var double avgTaTime
		if(phaseIs("active")){	
			if (!jobsSolved.isEmpty()) {
				avgTaTime = totalTa / jobsSolved.size()
				if (clock > 0.0) {
					throughput = jobsSolved.size() / clock
				} else {
					throughput = 0.0
				}
			} else {
				avgTaTime = 0.0
				throughput = 0.0
			}
			logger.info("End time: " + clock)
			logger.info("Jobs arrived : " + jobsArrived.size())
			logger.info("Jobs solved : " + jobsSolved.size())
			logger.info("Average TA = " + avgTaTime)
			logger.info("Throughput = " + throughput)
			passivate()
		}
	}
	
	override lambda() {
		if(phaseIs("active")){
			val job = new Job("null")
			oOut.addValue(job)	
		}
	}
	
}