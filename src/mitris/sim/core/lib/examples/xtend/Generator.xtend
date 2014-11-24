package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Atomic
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

class Generator extends Atomic {
	
	val protected InPort<Job> iStop = new InPort<Job>("iStop")
	val protected OutPort<Job> oOut = new OutPort<Job>("oOut")
	var int jobCounter
	var double period
	
	new(String name, double period) {
		super(name)
		addInPort(iStop)
		addOutPort(oOut)
		jobCounter =1
		this.period = period
		holdIn("active", period)
	}
	
	override deltext(double e) {
		passivate()
	}
	
	override deltint() {
		jobCounter++
		holdIn("active", period)
	}
	
	override lambda() {
		val job = new Job(""+jobCounter)
		oOut.addValue(job)
		
	}
	
}