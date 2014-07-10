/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import absys.logger.core.AbsysLogger;
import java.util.logging.Level;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.Coordinator;



/**
 *
 * @author jlrisco
 */
public class Efp extends Coupled {
    public Efp(double generatorPeriod, double processorPeriod, double transducerPeriod) {
        Ef ef = new Ef(generatorPeriod, transducerPeriod);
        addComponent(ef);
        Processor processor = new Processor(processorPeriod);
        addComponent(processor);
        addCoupling(ef, ef.oOut, processor, processor.iIn);
        addCoupling(processor, processor.oOut, ef, ef.iIn);
    }

    public static void main(String args[]) {
        AbsysLogger.setup(Level.INFO);
        Efp efp = new Efp(1, 3, 1000);
        Coordinator coordinator = new Coordinator(efp);
        coordinator.simulate(Long.MAX_VALUE);
    }
  
}
