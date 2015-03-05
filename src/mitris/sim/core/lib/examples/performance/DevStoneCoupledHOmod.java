package mitris.sim.core.lib.examples.performance;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.simulation.Coordinator;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHOmod extends DevStoneCoupled {

    private static final Logger logger = Logger.getLogger(DevStoneCoupledHOmod.class.getName());

    public InPort<Integer> iInAux = new InPort<>("inAux");

    public DevStoneCoupledHOmod(String prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime) {
        super(prefix + (depth - 1));
        super.addInPort(iInAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHOmod coupled = new DevStoneCoupledHOmod(prefix, width, depth - 1, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            // First layer of atomic models:
            ArrayList<DevStoneAtomic> prevLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL1_" + (i + 1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                super.addCoupling(atomic.oOut, coupled.iInAux);
                prevLayer.add(atomic);
            }
            // Second layer of atomic models:
            ArrayList<DevStoneAtomic> currentLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL2_" + (i + 1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
                super.addComponent(atomic);
                if (i == 0) {
                    super.addCoupling(iInAux, atomic.iIn);
                }
                currentLayer.add(atomic);
            }
            for (int i = 0; i < currentLayer.size(); ++i) {
                DevStoneAtomic fromAtomic = currentLayer.get(i);
                for (int j = 0; j < prevLayer.size(); ++j) {
                    DevStoneAtomic toAtomic = prevLayer.get(j);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
            }

            // Rest of the tree
            prevLayer = currentLayer;
            currentLayer = new ArrayList<>();
            int level = 3;
            while (prevLayer.size() > 1) {
                for (int i = 0; i < prevLayer.size() - 1; ++i) {
                    DevStoneAtomic atomic = new DevStoneAtomic("AL" + level + "_" + (i + 1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
                    super.addComponent(atomic);
                    if (i == 0) {
                        super.addCoupling(iInAux, atomic.iIn);
                    }
                    currentLayer.add(atomic);
                }
                for (int i = 0; i < currentLayer.size(); ++i) {
                    DevStoneAtomic fromAtomic = currentLayer.get(i);
                    DevStoneAtomic toAtomic = prevLayer.get(i + 1);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
                prevLayer = currentLayer;
                currentLayer.clear();
                level++;
            }
        }
    }

    public static void main(String[] args) {
        MitrisLogger.setup(Level.FINE);
        double preparationTime = 0.0;
        double period = 1.0;
        long maxEvents = 10;
        int width = 3;
        int depth = 3;
        double intDelayTime = 0;
        double extDelayTime = 0;
        Coupled framework = new Coupled("DevStoneHOmod");
        DevStoneGenerator generator = new DevStoneGenerator("Generator", preparationTime, period, maxEvents);
        framework.addComponent(generator);
        DevStoneCoupledHOmod coupled = new DevStoneCoupledHOmod("C", width, depth, preparationTime, intDelayTime, extDelayTime);
        framework.addComponent(coupled);
        framework.addCoupling(generator.oOut, coupled.iIn);
        framework.addCoupling(generator.oOut, coupled.iInAux);
        Coordinator coordinator = new Coordinator(framework, false);
        coordinator.initialize();
        long start = System.currentTimeMillis();
        coordinator.simulate(Long.MAX_VALUE);
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        logger.info("Execution time (PreparationTime, Period, MaxEvents, Width, Depth, IntDelayTime, ExtDelatTime) = (" + preparationTime + ", " + period + ", " + maxEvents + ", " + width + ", " + depth + ", " + intDelayTime + ", " + extDelayTime + ") = " + time);
    }
}
