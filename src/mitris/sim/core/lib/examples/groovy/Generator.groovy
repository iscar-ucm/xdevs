/**
 * 
 */
package mitris.sim.core.lib.examples.groovy

import mitris.sim.core.modeling.Atomic
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

/**
 * @author smittal
 *
 */
class Generator extends Atomic{

	protected InPort iStop = new InPort<Job>("iStop")
	protected OutPort oOut = new OutPort<Job>("oOut")
	int jobCounter
	double period
	
	Generator(String name, double period) {
		super(name)
		addInPort(iStop)
		addOutPort(oOut)
		jobCounter =1
		this.period = period
		holdIn("active", period)
	}
	
	@Override
	public void deltint() {
		jobCounter++
		holdIn("active", period)
	}

	@Override
	public void deltext(double e) {
		passivate()
	}

	@Override
	public void lambda() {
		Job job = new Job(""+jobCounter)
		oOut.addValue(job)
	}

}
