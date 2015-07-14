package xdevs.core.lib.examples;

import java.util.logging.Level;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;



/**
 *
 * @author jlrisco
 */
public class Efp extends Coupled {
	
	protected InPort<Job> iStart = new InPort<>("iStart");
	protected OutPort<Result> oResult = new OutPort<>("oResult");

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
        DevsLogger.setup(Level.FINE);
        Efp efp = new Efp("efp", 1, 3, 1000);
        Coordinator coordinator = new Coordinator(efp);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
    }
  
}
