package xdevs.lib.tmp_journal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;
import xdevs.core.util.RandomGenerator;
import xdevs.lib.performance.DevStone;
import xdevs.lib.performance.DevStoneAtomic;
import xdevs.lib.performance.DevStoneCoupledHO;
import xdevs.lib.performance.DevStoneGenerator;

/**
 * Este archivo se va a encargar de generar los XML para que se pueda realizar
 * la simulación distribuida de DevStone.
 */

public class DevStoneDistributed {
    private static final Logger LOGGER = Logger.getLogger(DevStoneDistributed.class.getName());

    private static final int MAX_EVENTS = 1;
    private static final double PREPARATION_TIME = 0.0;
    private static final double PERIOD = 1;
    private static final boolean RANDOM_GENERATOR = true;

    protected Boolean run = false;
    protected Boolean create = false;
    protected DevStone.BenchmarkType benchmarkType;
    protected int width = 0;
    protected int depth = 0;
    protected double maxDelayTime = 0;
    protected long seed = -1;
    protected String logPath = null;

    protected Coupled framework = null;
    protected DevStoneGenerator generator = null;
    protected DevStone stone = null;

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[] { "--run", "--create", "--model=HO", "--width=5", "--depth=5", "--max-delay-time=2.0",
                    "--seed=1234", "--log-filepath=ho-logger.log" };
        }
        DevStoneDistributed test = new DevStoneDistributed();
        for (String arg : args) {
            if (arg.equals("--run")) {
                test.run = true;
            } else if (arg.equals("--create")) {
                test.create = true;
            } else if (arg.startsWith("--model=")) {
                String[] parts = arg.split("=");
                test.benchmarkType = DevStone.BenchmarkType.valueOf(parts[1]);
            } else if (arg.startsWith("--width=")) {
                String[] parts = arg.split("=");
                test.width = Integer.parseInt(parts[1]);
            } else if (arg.startsWith("--depth=")) {
                String[] parts = arg.split("=");
                test.depth = Integer.parseInt(parts[1]);
            } else if (arg.startsWith("--max-delay-time=")) {
                String[] parts = arg.split("=");
                test.maxDelayTime = Double.parseDouble(parts[1]);
            } else if (arg.startsWith("--seed=")) {
                String[] parts = arg.split("=");
                test.seed = Long.parseLong(parts[1]);
            } else if (arg.startsWith("--log-filepath=")) {
                String[] parts = arg.split("=");
                test.logPath = parts[1];
            }
        }

        test.initialize();

        long simBegin = System.currentTimeMillis();

        test.buildFramework();
        test.runSimulation();

        long simEnds = System.currentTimeMillis();
        double simTime = (simEnds - simBegin) / 1e3;

        test.logReport(simTime);
        try {
            test.toXML();
        } catch (IOException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    public void initialize() {
        if (!run)
            return;
        // Suponemos que todos los argumentos son correctos, para simplificar
        RandomGenerator.setSeed(seed);
        DevStoneAtomic.NUM_DELT_INTS = 0;
        DevStoneAtomic.NUM_DELT_EXTS = 0;
        DevStoneAtomic.NUM_OF_EVENTS = 0;

    }

    public void runSimulation() {
        if (!run)
            return;
        DevsLogger.setup(logPath, Level.INFO);
        Coordinator coordinator = new Coordinator(framework, false);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();

    }

    public void buildFramework() {
        framework = new Coupled("DevStone" + benchmarkType.toString());
        generator = new DevStoneGenerator("Generator", PREPARATION_TIME, PERIOD, MAX_EVENTS);
        framework.addComponent(generator);
        switch (benchmarkType) {
            case HO:
                stone = new DevStoneCoupledHO("C", width, depth, PREPARATION_TIME, maxDelayTime, maxDelayTime,
                        RANDOM_GENERATOR);
                break;
            default:
                LOGGER.severe("Right now, only HO model is supported.");
                return;
        }
        framework.addComponent(stone);
        framework.addCoupling(generator.oOut, stone.iIn);
        switch (benchmarkType) {
            case HO:
                framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stone).iInAux);
                break;
            default:
                LOGGER.severe("Right now, only HO model is supported.");
                return;
        }
    }

    public void logReport(double simTime) {
        if (!run)
            return;
        // Theoretical values
        int numAtomics = stone.getNumOfAtomic(width, depth);
        int numDeltInts = stone.getNumDeltInts(MAX_EVENTS, width, depth);
        int numDeltExts = stone.getNumDeltExts(MAX_EVENTS, width, depth);
        long numOfEvents = stone.getNumOfEvents(MAX_EVENTS, width, depth);

        StringBuilder stats = new StringBuilder();
        stats.append("\n");
        stats.append("-------------------------------------------------------------\n");
        stats.append("MODEL=").append(benchmarkType.toString()).append("\n");
        stats.append("WIDTH=").append(Integer.toString(width)).append("\n");
        stats.append("DEPTH=").append(Integer.toString(depth)).append("\n");
        stats.append("MAX_EVENTS=").append(MAX_EVENTS).append("\n");
        stats.append("NUM_ATOMICS=").append(stone.countAtomicComponents()).append(" [").append(numAtomics)
                .append("]\n");
        stats.append("NUM_DELT_INTS=").append(DevStoneAtomic.NUM_DELT_INTS).append(" [").append(numDeltInts)
                .append("]\n");
        stats.append("NUM_DELT_EXTS=").append(DevStoneAtomic.NUM_DELT_EXTS).append(" [").append(numDeltExts)
                .append("]\n");
        stats.append("NUM_OF_EVENTS=").append(DevStoneAtomic.NUM_OF_EVENTS).append(" [").append(numOfEvents)
                .append("]\n");
        stats.append("SIM_TIME (s)=").append(simTime).append("\n");
        stats.append("-------------------------------------------------------------\n");

        LOGGER.info(stats.toString());
    }

    public void toXML() throws IOException {
        /**
         * TODO Hay que completar esto para que excriba bien el XML, DevStoneGenerator
         * incluido.
         * 
         * También hay que meter los argumentos de los constructores de los modelos
         * atómicos.
         * 
         * Para ello hay que crear una función toXML, específica de este problema.
         */
        if (!create)
            return;
        Coupled stoneFlattened = stone.flatten();
        String fileContent = stoneFlattened.getDistributedModel();
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                new File(benchmarkType + "_w-" + width + "_d-" + depth + "_t-" + maxDelayTime + ".xml")));
        writer.write(fileContent);
        writer.flush();
        writer.close();
    }
}