package mitris.sim.core.lib.examples.groovy

import java.util.logging.Logger
import mitris.sim.core.modeling.Atomic
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

class Transducer extends Atomic{

	static Logger logger = Logger.getLogger(Transducer.class.name)
	
	protected InPort iArrived = new InPort<Job> ("iArrived")
	protected InPort iSolved = new InPort<Job>("iSolved")
	protected OutPort oOut = new OutPort<Job>("oOut")
	
	LinkedList<Job> jobsArrived = new LinkedList<Job>()
	LinkedList<Job> jobsSolved = new LinkedList<Job>()
	
	double totalTa
	double clock
	
	Transducer (String name, double observationTime){
		super(name)
		totalTa = 0
		clock = 0
		addInPort(iArrived)
		addInPort(iSolved)
		addOutPort(oOut)
		holdIn("active", observationTime)
	}
	
	@Override
	public void deltint() {
		clock = clock + getSigma()
		double throughput
		double avgTaTime
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

	@Override
	public void deltext(double e) {
		clock = clock + e
		if(phaseIs("active")){
			Job job = null
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

	@Override
	public void lambda() {
		if(phaseIs("active")){
			Job job = new Job("null")
			oOut.addValue(job)	
		}
	}

}
