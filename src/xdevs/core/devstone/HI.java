package xdevs.core.devstone;

public class HI extends DEVStoneWrapper {

    public HI(String name, int depth, int width, int intDelay, int extDelay) {
        super(name, depth, width, intDelay, extDelay, true);


        if(this.components.size() > 1) {
            DummyAtomic atomic = ((DummyAtomic) this.components.get(this.components.size()-1));
            this.addCoupling(this.iIn, atomic.iIn);
        }

        for (int i = 1; i < this.components.size() - 1; i++) {
            DummyAtomic atomic = (DummyAtomic) this.components.get(i);
            DummyAtomic nextAtomic = (DummyAtomic) this.components.get(i+1);
            this.addCoupling(this.iIn, atomic.iIn);
            this.addCoupling(atomic.oOut, nextAtomic.iIn);
        }
    }

    @Override
    DEVStoneWrapper genCoupled() {
        return new HI("Coupled_" + (this.depth - 1), this.depth - 1, this.width, this.intDelay, this.extDelay);
    }
}