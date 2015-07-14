package xdevs.core.lib.examples.performance;

import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;

/**
 * Coupled model to study the performance using DEVStone
 *
 * @author José Luis Risco Martín
 */
public abstract class DevStoneCoupled extends Coupled {

    private static final Logger logger = Logger.getLogger(DevStoneCoupled.class.getName());

    public InPort<Integer> iIn = new InPort<>("in");
    public OutPort<Integer> oOut = new OutPort<>("out");

    public DevStoneCoupled(String name) {
        super(name);
        addInPort(iIn);
        addOutPort(oOut);
    }

    public abstract int getNumDeltExts(int maxEvents, int width, int depth);

    public abstract int getNumDeltInts(int maxEvents, int width, int depth);

    public abstract long getNumOfEvents(int maxEvents, int width, int depth);

    public static void main(String[] args) {
        DevStoneProperties properties = new DevStoneProperties();
        if (args.length == 1) {
            properties = new DevStoneProperties(args[0]);
        }
        else if(args.length>1) {
            properties.loadFromCommandLine(args);
        }
        DevsLogger.setup(properties.getProperty(DevStoneProperties.LOGGER_PATH), Level.INFO);
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
                        DevStoneAtomic.NUM_OF_EVENTS = 0;

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
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmem.toString())) {
                            stoneCoupled = new DevStoneCoupledHOmem("C", width, depth, properties);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmod.toString())) {
                            stoneCoupled = new DevStoneCoupledHOmod("C", width, depth, properties);
                        }
                        
                        // Theoretical values
                        int numDeltInts = stoneCoupled.getNumDeltInts(maxEvents, width, depth);
                        int numDeltExts = stoneCoupled.getNumDeltExts(maxEvents, width, depth);
                        long numOfEvents = stoneCoupled.getNumOfEvents(maxEvents, width, depth);
                        if(properties.getPropertyAsInteger(DevStoneProperties.MAX_NUMBER_OF_EVENTS)>0 && (numOfEvents/maxEvents)>properties.getPropertyAsInteger(DevStoneProperties.MAX_NUMBER_OF_EVENTS)) {
                            String stats = (currentTrial + 1) + ";" + maxEvents + ";" + width + ";" + depth + ";" + numDeltInts + ";" + numDeltExts + ";" + numOfEvents + ";-1.0";
                            logger.info(stats);
                            continue;
                        }

                        framework.addComponent(stoneCoupled);
                        framework.addCoupling(generator.oOut, stoneCoupled.iIn);
                        if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HO.toString())) {
                            framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stoneCoupled).iInAux);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmem.toString())) {
                            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmem) stoneCoupled).iInAux);
                        } else if (benchmarkName.equals(DevStoneProperties.BenchMarkType.HOmod.toString())) {
                            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmod) stoneCoupled).iInAux);
                        }
                        Coordinator coordinator = new Coordinator(framework, properties.getPropertyAsBoolean(DevStoneProperties.FLATTEN));
                        coordinator.initialize();
                        long start = System.currentTimeMillis();
                        coordinator.simulate(Long.MAX_VALUE);
                        long end = System.currentTimeMillis();
                        double time = (end - start) / 1000.0;
                        String stats;
                        if(DevStoneAtomic.NUM_DELT_INTS==numDeltInts && DevStoneAtomic.NUM_DELT_EXTS==numDeltExts && DevStoneAtomic.NUM_OF_EVENTS==numOfEvents) {
                            stats = (currentTrial + 1) + ";" + maxEvents + ";" + width + ";" + depth + ";" + DevStoneAtomic.NUM_DELT_INTS + ";" + DevStoneAtomic.NUM_DELT_EXTS + ";" + DevStoneAtomic.NUM_OF_EVENTS + ";" + time;
                        }
                        else {
                            stats = "ERROR: NumDeltInts or NumDeltExts or NumOfEvents do not match the theoretical values (between brackets): " + DevStoneAtomic.NUM_DELT_INTS + ";[" + numDeltInts + "];" + DevStoneAtomic.NUM_DELT_EXTS + ";[" + numDeltExts + "];" + DevStoneAtomic.NUM_OF_EVENTS + ";[" + numOfEvents + "];" + time;
                        }
                        logger.info(stats);
                    }
                }
            }
        }
    }
}
