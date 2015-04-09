package mitris.sim.core.lib.examples.performance;

import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHO extends DevStoneCoupled {

    public InPort<Integer> iInAux = new InPort<>("inAux");
    public OutPort<Integer> oOutAux = new OutPort<>("outAux");

    public DevStoneCoupledHO(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        super.addInPort(iInAux);
        super.addOutPort(oOutAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHO coupled = new DevStoneCoupledHO(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(iIn, coupled.iInAux);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                super.addCoupling(atomic.oOut, oOutAux);
                if (atomicPrev != null) {
                    super.addCoupling(atomicPrev.oOut, atomic.iIn);
                }
                atomicPrev = atomic;
            }
        }
    }

    @Override
    public int getNumDeltExts(int maxEvents, int width, int depth) {
        return maxEvents * (((width * width - width) / 2) * (depth - 1) + 1);

    }

    @Override
    public int getNumDeltInts(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }

    @Override
    public long getNumOfEvents(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }
}
