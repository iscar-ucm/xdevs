/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import java.util.logging.Level;

import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.Coordinator;

/**
 *
 * @author jlrisco
 */
public class Gpt extends Coupled {

    public Gpt(String name, double period, double observationTime) {
    	super(name);
        Generator generator = new Generator("generator", period);
        addComponent(generator);
        Processor processor = new Processor("processor", 3*period);
        addComponent(processor);
        Transducer transducer = new Transducer("transducer", observationTime);
        addComponent(transducer);

        addCoupling(generator, generator.oOut, processor, processor.iIn);
        addCoupling(generator, generator.oOut, transducer, transducer.iArrived);
        addCoupling(processor, processor.oOut, transducer, transducer.iSolved);
        addCoupling(transducer, transducer.oOut, generator, generator.iStop);
    }

    public static void main(String args[]) {
        MitrisLogger.setup(Level.INFO);
        Gpt gpt = new Gpt("gpt", 1, 10000);
        //CoordinatorParallel coordinator = new CoordinatorParallel(gpt);
        Coordinator coordinator = new Coordinator(gpt);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
    }

}
