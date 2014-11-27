package mitris.sim.core.lib.examples;

import java.util.LinkedList;
import java.util.logging.Logger;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;


/**
 *
 * @author José Luis Risco Martín
 * TODO: I keep the Transducer atomic model for the end ... 
 */
public class Transducer extends Atomic {

	private static final Logger logger = Logger.getLogger(Transducer.class.getName());

	protected InPort<Job> iArrived = new InPort<>("iArrived");
	protected InPort<Job> iSolved = new InPort<>("iSolved");
	protected OutPort<Job> oOut = new OutPort<>("oOut");
	protected OutPort<Job> oResult = new OutPort<>("oResult");
	
	protected LinkedList<Job> jobsArrived = new LinkedList<>();
	protected LinkedList<Job> jobsSolved = new LinkedList<>();
	protected double totalTa;
	protected double clock;
	protected double observationTime;
 	
	public Transducer(String name, double observationTime) {
		super(name);
		super.addInPort(iArrived);
		super.addInPort(iSolved);
		super.addOutPort(oOut);
		super.addOutPort(oResult);
		totalTa = 0;
		clock = 0;
		this.observationTime = observationTime;
		
	}

	@Override
	public void initialize() {
		super.holdIn("active", observationTime);
	}
	
	@Override
	public void deltint() {
		clock = clock + getSigma();
		double throughput;
		double avgTaTime;
		if (!jobsSolved.isEmpty()) {
			avgTaTime = totalTa / jobsSolved.size();
			if (clock > 0.0) {
				throughput = jobsSolved.size() / clock;
			} else {
				throughput = 0.0;
			}
		} else {
			avgTaTime = 0.0;
			throughput = 0.0;
		}
		logger.info("End time: " + clock);
		logger.info("Jobs arrived : " + jobsArrived.size());
		logger.info("Jobs solved : " + jobsSolved.size());
		logger.info("Average TA = " + avgTaTime);
		logger.info("Throughput = " + throughput);
		super.passivate();
	}

	@Override
	public void deltext(double e) {
		clock = clock + e;
		if(phaseIs("active")){
			Job job = null;
			if (!iArrived.isEmpty()) {
				job = iArrived.getSingleValue();
				logger.fine("Start job " + job.id + " @ t = " + clock);
				job.time = clock;
				jobsArrived.add(job);
			}
			if (!iSolved.isEmpty()) {
				job = iSolved.getSingleValue();
				totalTa += (clock - job.time);
				logger.fine("Finish job " + job.id + " @ t = " + clock);
				job.time = clock;
				jobsSolved.add(job);
			}
		}
	}

	@Override
	public void lambda() {
		if(phaseIs("done")){
			Job job = new Job("null");
			oOut.addValue(job);
			oResult.addValue(new Job("result"));
		}
		
	}
}
