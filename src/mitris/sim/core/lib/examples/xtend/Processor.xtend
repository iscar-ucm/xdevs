package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Atomic
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

class Processor extends Atomic{
	
	val protected InPort<Job> iIn = new InPort<Job>("iIn")
	val protected OutPort<Job> oOut = new OutPort<Job>("oOut")
	var Job currentJob
	var double processingTime
	
	new(String name, double processingTime){
		super(name)
		this.processingTime = processingTime
		addInPort(iIn)
		addOutPort(oOut)
		passivate();
	}
	
	override deltext(double e) {
		if(phaseIs("passive")){
			val job = iIn.singleValue
			currentJob = job
			holdIn("active", processingTime)
		}
	}
	
	override deltint() {
		passivate()
	}
	
	override lambda() {
		if(phaseIs("active")){
			oOut.addValue(currentJob)
		}
	}
	
}