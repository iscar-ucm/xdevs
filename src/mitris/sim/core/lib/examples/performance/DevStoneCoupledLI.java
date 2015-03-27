package mitris.sim.core.lib.examples.performance;

import java.util.logging.Logger;

/**
 * Coupled model to study the performance LI DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledLI extends DevStoneCoupled {
    private static final Logger logger = Logger.getLogger(DevStoneCoupledLI.class.getName());
    
    public DevStoneCoupledLI(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledLI coupled = new DevStoneCoupledLI(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i+1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iIn, atomic.iIn);
            }
        }
    }    
}
