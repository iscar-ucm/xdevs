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
class Efp extends Coupled {
	Efp(String name, double genPeriod, double procTime, double obsTime){
		super(name)
		
		Ef ef = new Ef("ef", genPeriod, obsTime)
		addComponent(ef)
		
		Processor proc = new Processor("processor", procTime)
		addComponent(proc)
		
		addCoupling(ef.oOut, proc.iIn)
		addCoupling(proc.oOut, ef.iIn)
	}
	
	def static void main(String... args){
		MitrisLogger.setup(Level.ALL);
		
		Efp efp = new Efp("Efp", 1, 3, 1000)
		Coordinator coordinator = new Coordinator(efp)
		coordinator.simulate(Long.MAX_VALUE)
	}
}
