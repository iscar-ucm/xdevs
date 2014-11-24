/**
 * 
 */
package mitris.sim.core.lib.examples.groovy

import mitris.sim.core.modeling.Coupled
import mitris.sim.core.modeling.InPort
import mitris.sim.core.modeling.OutPort

/**
 * @author smittal
 *
 */
class Ef extends Coupled{
	protected InPort iIn = new InPort<Job>("iIn")
	protected OutPort oOut = new OutPort<Job>("oOut")
	
	Ef(String name, double period, double observationTime){
		super(name)
		addInPort(iIn)
		addOutPort(oOut)
		
		Generator generator = new Generator("generator", period)
		addComponent(generator)
		
		Transducer transducer = new Transducer("transducer", observationTime)
		addComponent(transducer)
		
		addCoupling(generator.oOut, this.oOut)
		addCoupling(generator.oOut, transducer.iArrived)
		addCoupling(this.iIn, transducer.iSolved)
		addCoupling(transducer.oOut, generator.iStop)
	}
}
