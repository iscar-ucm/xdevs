package xdevs.core.devstone;

import xdevs.core.modeling.Port;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;

import java.util.logging.Level;

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

    public static void main(String[] args) {
        int depth = 10;
        int width = 10;
        if (args.length >= 2) {
            depth = Integer.parseInt(args[0]);
            width = Integer.parseInt(args[1]);
        }
        DevsLogger.setup(Level.SEVERE);
        HO ho = new HO("HO", depth, width, 0, 0);

        Coordinator coordinator = new Coordinator(ho, false);
        coordinator.initialize();
        coordinator.simInject(ho.iIn, 0);
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}