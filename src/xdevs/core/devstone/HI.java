package xdevs.core.devstone;

import xdevs.core.simulation.Coordinator;
import xdevs.core.test.TestingWrapper;
import xdevs.core.test.Transducer;
import xdevs.core.util.DevsLogger;

import java.util.logging.Level;

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

    public static void main(String[] args) {
        int depth = 10;
        int width = 10;
        if (args.length >= 2) {
            depth = Integer.parseInt(args[0]);
            width = Integer.parseInt(args[1]);
        }

        DevsLogger.setup(Level.SEVERE);
        HI hi = new HI("HI", depth, width, 0, 0);

        Coordinator coordinator = new Coordinator(hi, false);
        coordinator.initialize();
        coordinator.simInject(hi.iIn, 0);
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}