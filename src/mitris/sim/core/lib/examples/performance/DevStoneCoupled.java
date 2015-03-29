package mitris.sim.core.lib.examples.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import mitris.logger.core.MitrisLogger;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.simulation.Coordinator;

/**
 * Coupled model to study the performance using DEVStone
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupled extends Coupled {

    private static final Logger logger = Logger.getLogger(DevStoneCoupled.class.getName());

    public InPort<Integer> iIn = new InPort<>("in");
    public OutPort<Integer> oOut = new OutPort<>("out");

    public DevStoneCoupled(String name) {
        super(name);
        addInPort(iIn);
        addOutPort(oOut);
    }

    public static void main(String[] args) {
        DevStoneProperties properties = new DevStoneProperties();
        if (args.length == 1) {
            properties = new DevStoneProperties(args[0]);
        }
        MitrisLogger.setup(properties.getProperty(DevStoneProperties.LOGGER_PATH), Level.INFO);
        int numTrials = properties.getPropertyAsInteger(DevStoneProperties.NUM_TRIALS);
        int[] widthsAsArray = properties.getPropertyAsArrayOfInteger(DevStoneProperties.WIDTH);
        int[] depthsAsArray = properties.getPropertyAsArrayOfInteger(DevStoneProperties.DEPTH);
        int[] maxEventsAsArray = properties.getPropertyAsArrayOfInteger(DevStoneProperties.GENERATOR_MAX_EVENTS);
        for (int depth = depthsAsArray[0]; depth < depthsAsArray[2]; depth += depthsAsArray[1]) {
            for (int width = widthsAsArray[0]; width < widthsAsArray[2]; width += widthsAsArray[1]) {
                for (int maxEvents = maxEventsAsArray[0]; maxEvents < maxEventsAsArray[2]; maxEvents += maxEventsAsArray[1]) {
                    for (int currentTrial = 0; currentTrial < numTrials; ++currentTrial) {
                        DevStoneAtomic.NUM_DELT_INTS = 0;
                        DevStoneAtomic.NUM_DELT_EXTS = 0;
                        
                        Coupled framework = new Coupled("DevStone" + properties.getProperty(DevStoneProperties.BENCHMARK_NAME));

                        DevStoneGenerator generator = new DevStoneGenerator("Generator", properties, maxEvents);
                        framework.addComponent(generator);

                        DevStoneCoupled stoneCoupled = null;
                        String benchmarkName = properties.getProperty(DevStoneProperties.BENCHMARK_NAME);
                        if (benchmarkName.equals(DevStoneProperties.BenchMarkType.LI.toString())) {
                            stoneCoupled = new DevStoneCoupledLI("C", width, depth, properties);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HI.toString())) {
                            stoneCoupled = new DevStoneCoupledHI("C", width, depth, properties);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HO.toString())) {
                            stoneCoupled = new DevStoneCoupledHO("C", width, depth, properties);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmod.toString())) {
                            stoneCoupled = new DevStoneCoupledHOmod("C", width, depth, properties);
                        }
                        framework.addComponent(stoneCoupled);
                        framework.addCoupling(generator.oOut, stoneCoupled.iIn);
                        if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HO.toString())) {
                            framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stoneCoupled).iInAux);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmod.toString())) {
                            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmod) stoneCoupled).iInAux);
                        }
                        Coordinator coordinator = new Coordinator(framework, false);
                        coordinator.initialize();
                        long start = System.currentTimeMillis();
                        coordinator.simulate(Long.MAX_VALUE);
                        long end = System.currentTimeMillis();
                        double time = (end - start) / 1000.0;
                        int numDeltInts = 0;
                        int numDeltExts = 0;
                        if (benchmarkName.equals(DevStoneProperties.BenchMarkType.LI.toString())) {
                            numDeltInts = maxEvents * ((width - 1) * (depth - 1) + 1);
                            numDeltExts = numDeltInts;
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HI.toString())) {
                            numDeltInts = maxEvents * (((width * width - width) / 2) * (depth - 1) + 1);
                            numDeltExts = numDeltInts;
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HO.toString())) {
                            numDeltInts = maxEvents * (((width * width - width) / 2) * (depth - 1) + 1);
                            numDeltExts = numDeltInts;
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmod.toString())) {

                            numDeltInts = maxEvents * (depth * (depth - 1) * 3 * width * (width - 1) / 4 + 1);
                            numDeltExts = numDeltInts;
                        }
                        String stats = (currentTrial + 1) + ";" + maxEvents + ";" + width + ";" + depth + ";" + DevStoneAtomic.NUM_DELT_INTS + ";[" + numDeltInts + "];" + DevStoneAtomic.NUM_DELT_EXTS + ";[" + numDeltExts + "];" + time;
                        logger.info(stats);
                    }
                }
            }
        }
    }
}
