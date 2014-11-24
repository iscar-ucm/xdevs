/**
 * 
 */
package mitris.sim.core.lib.examples.groovy

import mitris.sim.core.modeling.Coupled
import mitris.logger.core.MitrisLogger
import java.util.logging.Level
import mitris.sim.core.simulation.Coordinator
/**
 * @author smittal
 *
 */
class Gpt extends Coupled{

	Gpt(String name, double period, double procTime, double obsTime){
		super(name)
		
		Generator generator = new Generator("generator", period)
		addComponent(generator)
		
		Processor processor = new Processor("processor", procTime)
		addComponent(processor)
		
		Transducer transducer = new Transducer("transducer", obsTime)
		addComponent(transducer)
		
		addCoupling(generator.oOut, processor.iIn)
		addCoupling(generator.oOut, transducer.iArrived)
		addCoupling(processor.oOut, transducer.iSolved)
		addCoupling(transducer.oOut, generator.iStop)
	}
	
	def static void main(String... args){
		MitrisLogger.setup(Level.FINE);
		Gpt gpt = new Gpt("gpt", 1, 3, 10000);
		//CoordinatorParallel coordinator = new CoordinatorParallel(gpt);
		Coordinator coordinator = new Coordinator(gpt);
		coordinator.simulate(Long.MAX_VALUE);
	}
}
