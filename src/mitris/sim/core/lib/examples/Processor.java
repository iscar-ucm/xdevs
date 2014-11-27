/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 *
 * @author jlrisco
 */
public class Processor extends Atomic {

	protected InPort<Job> iIn = new InPort<>("iIn");
	protected OutPort<Job> oOut = new OutPort<>("oOut");
	protected Job currentJob = null;
	protected double processingTime;

	public Processor(String name, double processingTime) {
		super(name);
		super.addInPort(iIn);
		super.addOutPort(oOut);
		this.processingTime = processingTime;
	}

	@Override
	public void initialize() {
		super.passivate();
	}

	@Override
	public void deltint() {
		super.passivate();
	}

	@Override
	public void deltext(double e) {
		if (super.phaseIs("passive")) {
			Job job = iIn.getSingleValue();
			currentJob = job;
			super.holdIn("active", processingTime);
		}
	}

	@Override
	public void lambda() {
		oOut.addValue(currentJob);
	}
}
