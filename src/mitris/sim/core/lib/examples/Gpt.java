/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import absys.logger.core.AbsysLogger;
import java.util.logging.Level;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.Coordinator;
import mitris.sim.core.simulation.CoordinatorParallel;
import mitris.sim.core.simulation.SimulationClock;

/**
 *
 * @author jlrisco
 */
public class Gpt extends Coupled {

    public Gpt(double period, double observationTime) {
        Generator generator = new Generator(period);
        addComponent(generator);
        Processor processor = new Processor(3*period);
        addComponent(processor);
        Transducer transducer = new Transducer(observationTime);
        addComponent(transducer);

        addCoupling(generator, generator.oOut, processor, processor.iIn);
        addCoupling(generator, generator.oOut, transducer, transducer.iArrived);
        addCoupling(processor, processor.oOut, transducer, transducer.iSolved);
        addCoupling(transducer, transducer.oOut, generator, generator.iStop);
    }

    public static void main(String args[]) {
        AbsysLogger.setup(Level.INFO);
        Gpt gpt = new Gpt(1, 10000);
        CoordinatorParallel coordinator = new CoordinatorParallel(gpt);
        //Coordinator coordinator = new Coordinator(gpt);
        coordinator.simulate(Long.MAX_VALUE);
    }

}
