package xdevs.core.devstone;

import xdevs.core.simulation.Coordinator;
import xdevs.core.test.TestingWrapper;
import xdevs.core.test.Transducer;
import xdevs.core.util.DevsLogger;

import java.util.logging.Level;

public class LI extends DEVStoneWrapper {

    public LI(String name, int depth, int width, int intDelay, int extDelay) {
        super(name, depth, width, intDelay, extDelay, true);

        for (int i = 1; i < this.components.size(); i++) {
            DummyAtomic atomic = (DummyAtomic) this.components.get(i);
            this.addCoupling(this.iIn, atomic.iIn);
        }
    }

    @Override
    DEVStoneWrapper genCoupled() {
        return new LI("Coupled_" + (this.depth - 1), this.depth - 1, this.width, this.intDelay, this.extDelay);
    }

    public static void main(String[] args) {
        int depth = 10;
        int width = 10;
        if (args.length >= 2) {
            depth = Integer.parseInt(args[0]);
            width = Integer.parseInt(args[1]);
        }
        DevsLogger.setup(Level.SEVERE);
        LI li = new LI("LI", depth, width, 0, 0);

        TestingWrapper wrapper = new TestingWrapper(li);
        Transducer<Integer> transducer = new Transducer<>("transducer");
        wrapper.addTransducer(li.oOut, transducer);

        Coordinator coordinator = new Coordinator(li, false);
        coordinator.initialize();
        coordinator.simInject(li.iIn, 0);
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}
