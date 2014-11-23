package mitris.sim.core.lib.examples;

import java.util.logging.Level;

import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.Coordinator;



/**
 *
 * @author jlrisco
 */
public class Efp extends Coupled {
    public Efp(String name, double generatorPeriod, double processorPeriod, double transducerPeriod) {
    	super(name);
        Ef ef = new Ef("ef", generatorPeriod, transducerPeriod);
        addComponent(ef);
        Processor processor = new Processor("processor", processorPeriod);
        addComponent(processor);
        addCoupling(ef, ef.oOut, processor, processor.iIn);
        addCoupling(processor, processor.oOut, ef, ef.iIn);
    }

    public static void main(String args[]) {
        MitrisLogger.setup(Level.INFO);
        Efp efp = new Efp("efp", 1, 3, 1000);
        Coordinator coordinator = new Coordinator(efp);
        coordinator.simulate(Long.MAX_VALUE);
    }
  
}
