package xdevs.lib.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.simulation.profile.CoordinatorProfile;
import xdevs.core.util.DevsLogger;

public class DevStoneSimulation {
    private static final Logger LOGGER = Logger.getLogger(DevStoneSimulation.class.getName());

    private static final int MAX_EVENTS = 1;
    private static final double PREPARATION_TIME = 0.0;
    private static final double PERIOD = 1;

    public static enum BenchmarkType {
        LI, HI, HO, HOmem, HOmod
    };

    protected BenchmarkType model = null;
    protected Integer width = null;
    protected Integer depth = null;
    protected String delayDistribution = "Constant-0";
    protected Long seed = null;
    protected String coordinatorAsString = null;
    protected Integer numThreads = null;
    protected Boolean flattened = Boolean.FALSE;
    protected String loadXml = null;
    protected String saveXml = null;
    protected String logPath = null;

    protected RealDistribution distribution = null;
    protected Coupled framework = null;
    protected DevStone stone = null;

    public static void main(String[] args) {
        // ARGUMENTS
        // ================================================================================
        if (args.length == 0) {
            System.err.println("Invalid number of arguments.");
            printUsage();
            return;
        }
        DevStoneSimulation simulation = new DevStoneSimulation();
        simulation.parseArguments(args);
        simulation.initDistribution();
        simulation.initLogger();

        // MODEL CREATION
        // ===========================================================================
        long modelStart = System.currentTimeMillis();
        simulation.buildFramework();
        long modelStop = System.currentTimeMillis();
        double modelCreationTime = ((modelStop - modelStart) / 1e3);

        // FLATTEN
        // ==================================================================================
        simulation.flatten();

        // SIMULATION
        // ===============================================================================
        simulation.runSimulation(modelCreationTime);

        // SAVE
        // =====================================================================================
        simulation.saveXml();
    }

    public static void printUsage() {
        System.err.println(
                "Usage: DevStone --model=model --width=width --depth=depth [--delay-distribution=distribution] [--seed=seed] [--coordinator=coordinator] [--num-threads=n] [--flattened] [--load-xml=path] [--save-xml=path] [--loger-path=path]");
        System.err.println("    --model: DEVStone model (LI, HI, HO, or HOmod)");
        System.err.println("    --width: DEVStone model's width (it must be an integer)");
        System.err.println("    --depth: DEVStone model's depth (it must be an integer)");
        System.err.println(
                "    --delay-distribution: Distribution used to compute transition delays. Possible values are Constant-V, which is a Constant distribution with fixed value V. ChiSquaredDistribution-V, with parameter V, and UniformRealDistribution-L-U that is a value between L and U. Default is Constant-0.");
        System.err.println("    --seed: Seed for the distribution (Long), f.i. 1234");
        System.err.println(
                "    --coordinator: Coordinator used for the simulation. Possible values are CoordinatorProfile, Coordinator, and CoordinatorParallel.");
        System.err.println(
                "    --num-threads: Number of threads used in the parallel coordinator. By default, the value is equal to the number of cores.");
        System.err.println("    --flattened: if present, flattens the model.");
        System.err.println("    --save-xml: saves an XML file with the model defined.");
        System.err.println("    --logger-path: path where the logger will be saved.");
    }

    private void buildFramework() {
        if (loadXml != null) {
            try {
                File file = new File(loadXml);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document docApplication = builder.parse(file.toURI().toString());
                Element xmlCoupled = (Element) docApplication.getElementsByTagName("coupled").item(0);
                framework = new Coupled(xmlCoupled);
            } catch (ParserConfigurationException e) {
                LOGGER.severe(e.getLocalizedMessage());
            } catch (SAXException e) {
                LOGGER.severe(e.getLocalizedMessage());
            } catch (IOException e) {
                LOGGER.severe(e.getLocalizedMessage());
            }
            return;
        }

        framework = new Coupled("DevStone" + model.toString());
        DevStoneGenerator generator = new DevStoneGenerator("Generator", PREPARATION_TIME, PERIOD, MAX_EVENTS);
        framework.addComponent(generator);
        switch (model) {
        case LI:
            stone = (distribution != null) ? new DevStoneCoupledLI("C", width, depth, PREPARATION_TIME, distribution)
                    : new DevStoneCoupledLI("C", width, depth, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                            Double.parseDouble(delayDistribution));
            break;
        case HI:
            stone = (distribution != null) ? new DevStoneCoupledHI("C", width, depth, PREPARATION_TIME, distribution)
                    : new DevStoneCoupledHI("C", width, depth, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                            Double.parseDouble(delayDistribution));
            break;
        case HO:
            stone = (distribution != null) ? new DevStoneCoupledHO("C", width, depth, PREPARATION_TIME, distribution)
                    : new DevStoneCoupledHO("C", width, depth, PREPARATION_TIME, Double.parseDouble(delayDistribution),
                            Double.parseDouble(delayDistribution));
            break;
        case HOmod:
            stone = (distribution != null) ? new DevStoneCoupledHOmod("C", width, depth, PREPARATION_TIME, distribution)
                    : new DevStoneCoupledHOmod("C", width, depth, PREPARATION_TIME,
                            Double.parseDouble(delayDistribution), Double.parseDouble(delayDistribution));
            break;
        default:
            LOGGER.severe(String.format("Model not supported: {}", model.toString()));
            return;
        }
        framework.addComponent(stone);
        framework.addCoupling(generator.oOut, stone.iIn);
        switch (model) {
        case HO:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stone).iInAux);
            break;
        case HOmem:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmem) stone).iInAux);
            break;
        case HOmod:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmod) stone).iInAux);
            break;
        default:
            LOGGER.severe(String.format("Model not supported: {}", model.toString()));
        }
    }

    private void parseArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--model=")) {
                String[] parts = arg.split("=");
                model = BenchmarkType.valueOf(parts[1]);
            } else if (arg.startsWith("--width=")) {
                String[] parts = arg.split("=");
                width = Integer.parseInt(parts[1]);
            } else if (arg.startsWith("--depth=")) {
                String[] parts = arg.split("=");
                depth = Integer.parseInt(parts[1]);
            } else if (arg.startsWith("--delay-distribution=")) {
                String[] parts = arg.split("=");
                delayDistribution = parts[1];
            } else if (arg.startsWith("--seed=")) {
                String[] parts = arg.split("=");
                seed = Long.parseLong(parts[1]);
            } else if (arg.startsWith("--coordinator=")) {
                String[] parts = arg.split("=");
                coordinatorAsString = parts[1];
            } else if (arg.startsWith("--num-threads=")) {
                String[] parts = arg.split("=");
                numThreads = Integer.parseInt(parts[1]);
            } else if (arg.startsWith("--flattened")) {
                flattened = Boolean.TRUE;
            } else if (arg.startsWith("--load-xml=")) {
                String[] parts = arg.split("=");
                loadXml = parts[1];
            } else if (arg.startsWith("--save-xml=")) {
                String[] parts = arg.split("=");
                saveXml = parts[1];
            } else if (arg.startsWith("--log-filepath=")) {
                String[] parts = arg.split("=");
                logPath = parts[1];
            }
        }
    }

    private void initDistribution() {
        // DISTRIBUTION
        // =============================================================================
        distribution = null;
        String[] parts = delayDistribution.split("-");
        if (parts[0].equals("UniformRealDistribution")) {
            distribution = new UniformRealDistribution(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } else if (parts[0].equals("ChiSquaredDistribution")) {
            distribution = new ChiSquaredDistribution(Integer.parseInt(parts[1]));
        } else if (parts[0].equals("Constant")) {
            distribution = null;
            delayDistribution = parts[1];
        }
        if (distribution != null && seed != null)
            distribution.reseedRandomGenerator(seed);
    }

    private void initLogger() {
        if (logPath != null) {
            DevsLogger.setup(logPath, Level.INFO);
        } else {
            DevsLogger.setup(Level.INFO);
        }
    }

    private void flatten() {
        if (flattened.equals(Boolean.TRUE)) {
            framework = framework.flatten();
        }
    }

    private void runSimulation(double modelCreationTime) {
        if (coordinatorAsString == null)
            return;
        DevStoneAtomic.NUM_DELT_INTS = 0;
        DevStoneAtomic.NUM_DELT_EXTS = 0;
        DevStoneAtomic.NUM_OF_EVENTS = 0;
        Coordinator coordinator = null;
        long coordStart = System.currentTimeMillis();
        if (coordinatorAsString.equals("CoordinatorProfile")) {
            coordinator = new CoordinatorProfile(framework);
        } else if (coordinatorAsString.equals("Coordinator")) {
            coordinator = new Coordinator(framework);
        } else if (coordinatorAsString.equals("CoordinatorParallel")) {
            if (numThreads != null) {
                coordinator = new CoordinatorParallel(framework, numThreads);
            } else {
                coordinator = new CoordinatorParallel(framework);
            }
        }
        coordinator.initialize();
        long coordStop = System.currentTimeMillis();
        double engineSetupTime = ((coordStop - coordStart) / 1e3);
        // Theoretical values
        int numDeltInts = 0, numDeltExts = 0;
        long numOfEvents = 0;
        if (stone != null) {
            numDeltInts = stone.getNumDeltInts(MAX_EVENTS, width, depth);
            numDeltExts = stone.getNumDeltExts(MAX_EVENTS, width, depth);
            numOfEvents = stone.getNumOfEvents(MAX_EVENTS, width, depth);
        }
        long simulationStart = System.currentTimeMillis();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        long simulationStop = System.currentTimeMillis();
        double simulationTime = (simulationStop - simulationStart) / 1e3;
        LOGGER.info(
                "MODEL,MAXEVENTS,WIDTH,DEPTH,NUM_DELT_INTS,NUM_DELT_EXTS,NUM_OF_EVENTS,SIMULATION_TIME,MODEL_CREATION_TIME,ENGINE_SETUP_TIME");
        String stats = "";
        if (DevStoneAtomic.NUM_DELT_INTS != numDeltInts || DevStoneAtomic.NUM_DELT_EXTS != numDeltExts
                || DevStoneAtomic.NUM_OF_EVENTS != numOfEvents) {
            DevStoneAtomic.NUM_DELT_INTS = -DevStoneAtomic.NUM_DELT_INTS;
            DevStoneAtomic.NUM_DELT_EXTS = -DevStoneAtomic.NUM_DELT_EXTS;
            DevStoneAtomic.NUM_OF_EVENTS = -DevStoneAtomic.NUM_OF_EVENTS;
        }
        if (stone != null) {
            stats = model.toString() + "," + MAX_EVENTS + "," + width + "," + depth + "," + DevStoneAtomic.NUM_DELT_INTS
                    + "," + DevStoneAtomic.NUM_DELT_EXTS + "," + DevStoneAtomic.NUM_OF_EVENTS + "," + simulationTime
                    + "," + modelCreationTime + "," + engineSetupTime;
        } else {
            stats = loadXml + "," + MAX_EVENTS + "," + 0 + "," + 0 + "," + -DevStoneAtomic.NUM_DELT_INTS + ","
                    + -DevStoneAtomic.NUM_DELT_EXTS + "," + -DevStoneAtomic.NUM_OF_EVENTS + "," + simulationTime + ","
                    + modelCreationTime + "," + engineSetupTime;

        }
        LOGGER.info(stats);
    }

    private void saveXml() {
        if (saveXml == null)
            return;
        try {
            var writer = new BufferedWriter(new FileWriter(new File(saveXml)));
            writer.write(framework.toXml());
            writer.close();
        } catch (IOException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }

    }
}
