package mitris.sim.core.lib.examples.performance;

import java.util.ArrayList;
import mitris.sim.core.modeling.InPort;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHOmem extends DevStoneCoupled {

    public InPort<Integer> iInAux = new InPort<>("inAux");

    public DevStoneCoupledHOmem(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        super.addInPort(iInAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHOmem coupled = new DevStoneCoupledHOmem(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            // First layer of atomic models:
            ArrayList<DevStoneAtomic> prevLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL1_" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(atomic.oOut, coupled.iInAux);
                prevLayer.add(atomic);
            }
            // Second layer of atomic models:
            ArrayList<DevStoneAtomic> currentLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL2_" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                currentLayer.add(atomic);
            }
            for (int i = 0; i < currentLayer.size(); ++i) {
                DevStoneAtomic fromAtomic = currentLayer.get(i);
                for (int j = 0; j < prevLayer.size(); ++j) {
                    DevStoneAtomic toAtomic = prevLayer.get(j);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
            }
        }
    }
}
