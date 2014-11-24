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
class Processor extends Atomic{

	protected InPort iIn = new InPort<Job>("iIn")
	protected OutPort oOut = new OutPort<Job>("oOut")
	Job currentJob
	double processingTime

	Processor(String name, double processingTime){
		super(name)
		this.processingTime = processingTime
		addInPort(iIn)
		addOutPort(oOut)
		passivate();
	}
	
	@Override
	public void deltint() {
		passivate()
	}

	@Override
	public void deltext(double e) {
		if(phaseIs("passive")){
			Job job = iIn.singleValue
			currentJob = job
			holdIn("active", processingTime)
		}
	}

	@Override
	public void lambda() {
		if(phaseIs("active")){
			oOut.addValue(currentJob)
		}
	}

}
