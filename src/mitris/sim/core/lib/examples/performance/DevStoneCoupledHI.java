package mitris.sim.core.lib.examples.performance;

import java.util.logging.Level;
import java.util.logging.Logger;
import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.Coordinator;

/**
 * Coupled model to study the performance HI DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHI extends DevStoneCoupled {
    private static final Logger logger = Logger.getLogger(DevStoneCoupledHI.class.getName());
    
    public DevStoneCoupledHI(String prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime) {
        super(prefix + (depth - 1));
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHI coupled = new DevStoneCoupledHI(prefix, width, depth - 1, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i+1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
                super.addComponent(atomic);
                super.addCoupling(iIn, atomic.iIn);
                if(atomicPrev!=null) {
                    super.addCoupling(atomicPrev.oOut, atomic.iIn);
                }
                atomicPrev = atomic;
            }
        }
    }
    
    public static void main(String[] args) {
        MitrisLogger.setup(Level.FINE);
        double preparationTime = 0.0;
        double period = 1.0;
        long maxEvents = 6000;
        int width = 3;
        int depth = 3;
        double intDelayTime = 0;
        double extDelayTime = 0;
        Coupled framework = new Coupled("DevStoneHI");
        DevStoneGenerator generator = new DevStoneGenerator("Generator", preparationTime, period, maxEvents);
        framework.addComponent(generator);
        DevStoneCoupledHI coupled = new DevStoneCoupledHI("C", width, depth, preparationTime, intDelayTime, extDelayTime);
        framework.addComponent(coupled);
        framework.addCoupling(generator.oOut, coupled.iIn);
        Coordinator coordinator = new Coordinator(framework, false);
        coordinator.initialize();
        long start = System.currentTimeMillis();
        coordinator.simulate(Long.MAX_VALUE);
        long end = System.currentTimeMillis();
        double time = (end-start)/1000.0;
        logger.info("Execution time (PreparationTime, Period, MaxEvents, Width, Depth, IntDelayTime, ExtDelatTime) = (" + preparationTime + ", " + period + ", " + maxEvents + ", " + width + ", " + depth + ", " + intDelayTime + ", " + extDelayTime + ") = " + time);
    }
}
