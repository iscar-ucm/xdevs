/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.DevsAtomic;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author José Luis Risco Martín
 * TODO: I must also modify this class, according to the source code implemented by Saurabh, a iStart input port must be added.
 */
public class Generator extends DevsAtomic {

	protected Port<Job> iStop = new Port<Job>("iStop");
	protected Port<Job> oOut = new Port<Job>("oOut");
	protected int jobCounter;
	protected double period;

	public Generator(String name, double period) {
		super(name);
		super.addInPort(iStop);
		super.addOutPort(oOut);
		this.period = period;
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
