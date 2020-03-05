package xdevs.core.devstone;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HOmod extends Coupled {

    protected int depth;
    protected int width;
    protected int intDelay;
    protected int extDelay;
    protected Port<Integer> iIn, iIn2, oOut;

    public HOmod(String name, int depth, int width, int intDelay, int extDelay) {
        super(name);

        if(depth < 1) throw new RuntimeException("Invalid depth");
        else if(width < 1) throw new RuntimeException("Invalid width");
        else if(intDelay < 0) throw new RuntimeException("Invalid internal delay");
        else if(extDelay < 0) throw new RuntimeException("Invalid external delay");

        this.depth = depth;
        this.width = width;
        this.intDelay = intDelay;
        this.extDelay = extDelay;

        this.iIn = new Port<>("iIn");
        this.addInPort(this.iIn);

        this.iIn2 = new Port<>("iIn2");
        this.addInPort(this.iIn2);

        this.oOut = new Port<>("oOut");
        this.addOutPort(this.oOut);

        if(depth == 1) {
            DummyAtomic atomic = new DummyAtomic("Atomic_0_0", intDelay, extDelay);
            this.addComponent(atomic);

            this.addCoupling(this.iIn, atomic.iIn);
            this.addCoupling(atomic.oOut, this.oOut);
        } else {
            HOmod coupled = new HOmod("Coupled_" + (depth - 1), depth, width, intDelay, extDelay);
            this.addComponent(coupled);

            this.addCoupling(this.iIn, coupled.iIn);
            this.addCoupling(coupled.oOut, this.oOut);

            if(width >= 2) {
                Map<Integer, List<DummyAtomic>> atomics = new HashMap<>();

                // Generate atomic components
                for (int i = 0; i < width; i++) {
                    int minRowIdx = (i < 2) ? 0 : (i-1);
                    for (int j = minRowIdx; j < width - 1; j++) {
                        DummyAtomic atomic = new DummyAtomic("Atomic_" + (depth - 1) + "_" + i + "_" + j, intDelay, extDelay);
                        this.addComponent(atomic);

                        if(!atomics.containsKey(i))
                            atomics.put(i, new LinkedList<>());
                        atomics.get(i).add(atomic);
                    }
                }

                // Connect EIC
                for (DummyAtomic atomic : atomics.get(0)) {
                    this.addCoupling(this.iIn2, atomic.iIn);
                }

                for (int i = 1; i < width; i++) {
                    this.addCoupling(this.iIn2, atomics.get(1).get(i).iIn);
                }

                // Connect IC
                for (DummyAtomic atomic : atomics.get(0)) {
                    this.addCoupling(atomic.oOut, coupled.iIn2);
                }
                for (int i = 0; i < atomics.get(1).size(); i++) {
                    DummyAtomic atomic = atomics.get(1).get(i);
                    DummyAtomic topAtomic = atomics.get(0).get(i);
                    this.addCoupling(atomic.oOut, topAtomic.iIn);
                }
                for (int i = 2; i < width; i++) {
                    for (int j = 0; j < atomics.get(i).size(); j++) {
                        this.addCoupling(atomics.get(i).get(j).oOut, atomics.get(i-1).get(j+1).iIn);
                    }
                }
            }
        }
    }
}
