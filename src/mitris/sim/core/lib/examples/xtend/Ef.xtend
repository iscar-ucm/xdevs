package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Coupled
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

class Ef extends Coupled{
	
	val protected InPort<Job> iIn = new InPort<Job>("iIn")
	val protected OutPort<Job> oOut = new OutPort<Job>("oOut")
	
	new(String name, double period, double observationTime){
		super(name)
		addInPort(iIn)
		addOutPort(oOut)
		
		val Generator generator = new Generator("generator", period)
		addComponent(generator)
		
		val transducer = new Transducer("transducer", observationTime)
		addComponent(transducer)
		
		addCoupling(generator.oOut, this.oOut)
		addCoupling(generator.oOut, transducer.iArrived)
		addCoupling(this.iIn, transducer.iSolved)
		addCoupling(transducer.oOut, generator.iStop)
	}
}