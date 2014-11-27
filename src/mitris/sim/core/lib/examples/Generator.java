package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 *
 * @author José Luis Risco Martín
 * TODO: I must also modify this class, according to the source code implemented by Saurabh, a iStart input port must be added.
 */
public class Generator extends Atomic {

	protected InPort<Job> iStart = new InPort<Job>("iStart");
	protected InPort<Job> iStop = new InPort<Job>("iStop");
	protected OutPort<Job> oOut = new OutPort<Job>("oOut");
	protected int jobCounter;
	protected double period;

	public Generator(String name, double period) {
		super(name);
		super.addInPort(iStop);
		super.addInPort(iStart);
		super.addOutPort(oOut);
		this.period = period;
	}

	public void initialize() {
		jobCounter = 1;
		this.holdIn("active", period);		
	}

	@Override
	public void deltint() {
		jobCounter++;
		this.holdIn("active", period);
	}

	@Override
	public void deltext(double e) {
		super.passivate();
	}

	@Override
	public void lambda() {
		Job job = new Job("" + jobCounter + "");
		oOut.addValue(job);
	}
}
