package mitris.sim.core.lib.examples;

import java.util.logging.Level;

import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.simulation.Coordinator;



/**
 *
 * @author jlrisco
 */
public class Efp extends Coupled {
	
	protected InPort<Job> iStart = new InPort<Job>("iStart");
	protected OutPort<Job> oResult = new OutPort<Job>("oResult");

    public Efp(String name, double generatorPeriod, double processorPeriod, double transducerPeriod) {
    	super(name);
    	addInPort(iStart);
    	addOutPort(oResult);
    	
        Ef ef = new Ef("ef", generatorPeriod, transducerPeriod);
        addComponent(ef);
        Processor processor = new Processor("processor", processorPeriod);
        addComponent(processor);
        
        addCoupling(ef.oOut, processor.iIn);
        addCoupling(processor.oOut, ef.iIn);
        addCoupling(this.iStart, ef.iStart);
        addCoupling(ef.oOut, this.oResult);
    }

    public static void main(String args[]) {
        MitrisLogger.setup(Level.FINE);
        Efp efp = new Efp("efp", 1, 3, 1000);
        Coordinator coordinator = new Coordinator(efp);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
    }
  
}
