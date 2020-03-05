package xdevs.core.devstone;

import xdevs.core.modeling.Component;

public class LI extends DEVStoneWrapper {

    public LI(String name, int depth, int width, int intDelay, int extDelay) {
        super(name, depth, width, intDelay, extDelay, false);

        for (int i = 1; i < this.components.size(); i++) {
            DummyAtomic atomic = (DummyAtomic) this.components.get(i);
            this.addCoupling(this.iIn, atomic.iIn);
        }
    }

    @Override
    DEVStoneWrapper genCoupled() {
        return new LI("Coupled_" + (this.depth - 1), this.depth, this.width, this.intDelay, this.extDelay);
    }
}
