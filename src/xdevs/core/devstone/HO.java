package xdevs.core.devstone;

import xdevs.core.modeling.Port;

public class HO extends DEVStoneWrapper {

    protected Port<Integer> iIn2, oOut2;

    public HO(String name, int depth, int width, int intDelay, int extDelay) {
        super(name, depth, width, intDelay, extDelay, true);

        this.iIn2 = new Port<>("iIn2");
        this.addInPort(this.iIn2);

        this.oOut2 = new Port<>("oOut2");
        this.addOutPort(this.oOut2);

        if(depth != 1) {
            DEVStoneWrapper coupled = (DEVStoneWrapper) this.components.get(0);
            this.addCoupling(this.iIn, coupled.iIn);
        }

        if(this.components.size() > 1) {
            DummyAtomic atomic = ((DummyAtomic) this.components.get(this.components.size()-1));
            this.addCoupling(this.iIn2, atomic.iIn);
            this.addCoupling(atomic.oOut, this.oOut2);
        }

        for (int i = 1; i < this.components.size() - 1; i++) {
            DummyAtomic atomic = (DummyAtomic) this.components.get(i);
            DummyAtomic nextAtomic = (DummyAtomic) this.components.get(i+1);
            this.addCoupling(this.iIn2, atomic.iIn);
            this.addCoupling(atomic.oOut, nextAtomic.iIn);
            this.addCoupling(atomic.oOut, this.oOut2);
        }
    }

    @Override
    DEVStoneWrapper genCoupled() {
        return new HO("Coupled_" + (this.depth - 1), this.depth - 1, this.width, this.intDelay, this.extDelay);
    }
}