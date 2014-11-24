package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Coupled
import mitris.logger.core.MitrisLogger
import java.util.logging.Level
import mitris.sim.core.simulation.Coordinator

class Gpt extends Coupled{
	
	new(String name, double period, double observationTime){
		super(name)
		
		val Generator generator = new Generator("generator", period)
		addComponent(generator)
		
		val processor = new Processor("processor", 3*period)
		addComponent(processor)
		
		val transducer = new Transducer("transducer", observationTime)
		addComponent(transducer)
		
		addCoupling(generator.oOut, processor.iIn)
		addCoupling(generator.oOut, transducer.iArrived)
		addCoupling(processor.oOut, transducer.iSolved)
		addCoupling(transducer.oOut, generator.iStop)
	}
	
	def static void main(String... args){
		MitrisLogger.setup(Level.INFO);
        val gpt = new Gpt("gpt", 1, 10000);
        //CoordinatorParallel coordinator = new CoordinatorParallel(gpt);
        val coordinator = new Coordinator(gpt);
        coordinator.simulate(Long.MAX_VALUE);
	}
}