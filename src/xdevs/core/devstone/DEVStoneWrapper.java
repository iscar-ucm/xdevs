package xdevs.core.devstone;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

/**
 * Auxiliar class to simplify the construction of LI, HI and HO models
 */
abstract class DEVStoneWrapper extends Coupled {

    protected int depth;
    protected int width;
    protected int intDelay;
    protected int extDelay;
    protected boolean addAtomicOutPorts;
    protected Port<Integer> iIn, oOut;

    public DEVStoneWrapper(String name, int depth, int width, int intDelay, int extDelay, boolean addAtomicOutPorts) {
        this(name, depth, width, intDelay, extDelay, addAtomicOutPorts, false);
    }

    public DEVStoneWrapper(String name, int depth, int width, int intDelay, int extDelay, boolean addAtomicOutPorts, boolean stats) {
        super(name);

        if(depth < 1) throw new RuntimeException("Invalid depth");
        else if(width < 1) throw new RuntimeException("Invalid width");
        else if(intDelay < 0) throw new RuntimeException("Invalid internal delay");
        else if(extDelay < 0) throw new RuntimeException("Invalid external delay");

        this.depth = depth;
        this.width = width;
        this.intDelay = intDelay;
        this.extDelay = extDelay;
        this.addAtomicOutPorts = addAtomicOutPorts;

        this.iIn = new Port<>("iIn");
        this.addInPort(this.iIn);

        this.oOut = new Port<>("oOut");
        this.addOutPort(this.oOut);

        if(depth == 1) {
            DummyAtomic atomic;
            if (stats) {
                atomic = new DummyAtomicStats("Atomic_0_0", intDelay, extDelay);
            } else {
                atomic = new DummyAtomic("Atomic_0_0", intDelay, extDelay);
            }
            this.addComponent(atomic);

            this.addCoupling(this.iIn, atomic.iIn);
            this.addCoupling(atomic.oOut, this.oOut);
        } else {
            DEVStoneWrapper coupled = genCoupled();
            this.addComponent(coupled);

            this.addCoupling(this.iIn, coupled.iIn);
            this.addCoupling(coupled.oOut, this.oOut);

            for (int i = 0; i < width - 1; i++) {
                DummyAtomic atomic;
                if (stats) {
                    atomic = new DummyAtomicStats("Atomic_" + (depth - 1) + "_" + i, intDelay, extDelay, addAtomicOutPorts);
                } else {
                    atomic = new DummyAtomic("Atomic_" + (depth - 1) + "_" + i, intDelay, extDelay, addAtomicOutPorts);
                }
                this.addComponent(atomic);
            }
        }

    }

    /**
     * @return Coupled module with iIn and oOut ports
     */
    abstract DEVStoneWrapper genCoupled();
}
