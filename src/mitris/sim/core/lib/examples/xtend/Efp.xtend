package mitris.sim.core.lib.examples.xtend

import mitris.sim.core.modeling.Coupled
import mitris.logger.core.MitrisLogger
import java.util.logging.Level
import mitris.sim.core.simulation.Coordinator

class Efp extends Coupled{
	
	new(String name, double genPeriod, double procTime, double obsTime){
		super(name)
		
		val ef = new Ef("ef", genPeriod, obsTime)
		addComponent(ef)
		
		val proc = new Processor("processor", procTime)
		addComponent(proc)
		
		addCoupling(ef.oOut, proc.iIn)
		addCoupling(proc.oOut, ef.iIn)
	}
	
	def static void main(String... args){
		MitrisLogger.setup(Level.INFO);
        
        val efp = new Efp("Efp", 1, 3, 1000)
		val coordinator = new Coordinator(efp)
		coordinator.simulate(Long.MAX_VALUE)
	}
}