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
    public Efp() {
        Ef ef = new Ef(1, 100);
        addComponent(ef);
        Processor processor = new Processor(2);
        addComponent(processor);
        addCoupling(ef, ef.oOut, processor, processor.iIn);
        addCoupling(processor, processor.oOut, ef, ef.iIn);
    }

    public static void main(String args[]) {
        AbsysLogger.setup(Level.INFO);
        Efp efp = new Efp();
        Coordinator coordinator = new Coordinator(efp);
        coordinator.simulate(Long.MAX_VALUE);
    }
  
}
