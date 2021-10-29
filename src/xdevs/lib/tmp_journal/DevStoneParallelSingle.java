package xdevs.lib.tmp_journal;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.util.DevsLogger;
import xdevs.lib.performance.DevStoneAtomic;

/**
 *
 * @author jlrisco
 */
public class DevStoneParallelSingle extends CoordinatorParallel {

    private static final Logger LOGGER = Logger.getLogger(DevStoneParallelSingle.class.getName());

    public DevStoneParallelSingle(Coupled model, int numberOfThreads) {
        super(new SimulationClock(), model, numberOfThreads);
    }

    public static void printUsage() {
        System.err.println("Usage: java -cp " + DevStoneParallelSingle.class.getCanonicalName() + " <model-path> <nthreads>");
        System.err.println("    - <model>: DEVStone XML model file");
        System.err.println("    - <nthreads>: number of threads");
    }

    public static void main(String[] args) {
        if(args.length!=2) {
            // model, nthreads
            System.err.println("Invalid number of arguments.");
            printUsage();
            return;
        }
        DevsLogger.setup("ho15-chisquare2-" + args[1] + ".log", Level.INFO);

        Element xmlCoupled = null;
        File file = new File(args[0]);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document docApplication = builder.parse(file.toURI().toString());
            xmlCoupled = (Element) docApplication.getElementsByTagName("coupled").item(0);
        } catch (Exception ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }

        Coupled model = new Coupled(xmlCoupled);

        LOGGER.fine("Creating coordinator ...");
        DevStoneParallelSingle coordinator = new DevStoneParallelSingle(model, Integer.parseInt(args[1]));
        LOGGER.fine("Initializing coordinator ...");
        coordinator.initialize();
        long simBegin = System.currentTimeMillis();
        LOGGER.fine("Running simulation ...");
        coordinator.simulate(Long.MAX_VALUE);
        long simEnds = System.currentTimeMillis();
        double simTime = (simEnds - simBegin) / 1e3;
        coordinator.exit();

        LOGGER.fine("Printing reports ...");
        coordinator.logReport(model, simTime);
        LOGGER.fine("DONE.");
    }

    public void logReport(Coupled model, double simTime) {
        // Theoretical values
        int numAtomics = (15 - 1) * (15 - 1) + 1;
        numAtomics = numAtomics + 1; // Generator
        int numDeltExts = 1 * (((15 * 15 - 15) / 2) * (15 - 1) + 1);
        int numDeltInts = numDeltExts;
        long numOfEvents = numDeltExts;

        StringBuilder stats = new StringBuilder();
        stats.append("\n");
        stats.append("-------------------------------------------------------------\n");
        stats.append("MODEL=HO\n");
        stats.append("WIDTH=15\n");
        stats.append("DEPTH=15\n");
        stats.append("MAX_EVENTS=1\n");
        stats.append("NUM_ATOMICS=").append(model.countAtomicComponents()).append(" [").append(numAtomics)
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
}