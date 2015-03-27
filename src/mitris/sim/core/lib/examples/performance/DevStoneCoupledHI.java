package mitris.sim.core.lib.examples.performance;

/**
 * Coupled model to study the performance HI DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHI extends DevStoneCoupled {
    
    public DevStoneCoupledHI(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHI coupled = new DevStoneCoupledHI(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i+1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iIn, atomic.iIn);
                if(atomicPrev!=null) {
                    super.addCoupling(atomicPrev.oOut, atomic.iIn);
                }
                atomicPrev = atomic;
            }
        }
    }    
}
