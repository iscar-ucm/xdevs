package xdevs.lib.tmp_journal;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.parallel.TaskDeltFcn;
import xdevs.core.simulation.parallel.TaskLambda;
import xdevs.core.util.DevsLogger;
import xdevs.lib.performance.DevStoneAtomic;

/**
 *
 * @author jlrisco
 */
public class DevStoneParallelCoordinator extends Coordinator {

    private static final Logger LOGGER = Logger.getLogger(DevStoneParallelCoordinator.class.getName());

    protected int numberOfThreads;
    protected LinkedList<TaskLambda> lambdaSlowTasks = new LinkedList<>();
    protected LinkedList<TaskDeltFcn> deltfcnSlowTasks = new LinkedList<>();
    protected LinkedList<TaskLambda> lambdaFastTasks = new LinkedList<>();
    protected LinkedList<TaskDeltFcn> deltfcnFastTasks = new LinkedList<>();
    protected ExecutorService executorSlow;
    protected ExecutorService executorFast;

    protected HashSet<String> slowModels = new HashSet<>();
    protected HashSet<String> fastModels = new HashSet<>();

    public DevStoneParallelCoordinator(Coupled model, int numberOfSlowThreads, HashSet<String> slowModels, int numberOfFastThreads, HashSet<String> fastModels) {
        super(new SimulationClock(), model, true);
        this.numberOfThreads = numberOfSlowThreads+numberOfFastThreads;
        executorSlow = Executors.newFixedThreadPool(numberOfSlowThreads);
        this.slowModels = slowModels;
        executorFast = Executors.newFixedThreadPool(numberOfFastThreads);
        this.fastModels = fastModels;
    }

    @Override
    public void buildHierarchy() {
        super.buildHierarchy();
        simulators.forEach(simulator -> {
            Component component = simulator.getModel();
            if (slowModels.contains(component.getName())) {
                LOGGER.fine("Adding simulator " + component.getName() + " to the set of slow simulators.");
                lambdaSlowTasks.add(new TaskLambda(simulator));
                deltfcnSlowTasks.add(new TaskDeltFcn(simulator));                
            }
            else if (fastModels.contains(component.getName())) {
                LOGGER.fine("Adding simulator " + component.getName() + " to the set of fast simulators.");
                lambdaFastTasks.add(new TaskLambda(simulator));
                deltfcnFastTasks.add(new TaskDeltFcn(simulator));
            }
            else {
                LOGGER.severe("ERROR: The model " + component.getName() + "is not classified as slow nor fast.");
            }
        });
    }

    @Override
    public void lambda() {
        try {
            executorSlow.invokeAll(lambdaSlowTasks);
            executorFast.invokeAll(lambdaFastTasks);
        } catch (InterruptedException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        propagateOutput();
    }

    @Override
    public void deltfcn() {
        propagateInput();
        try {
            executorSlow.invokeAll(deltfcnSlowTasks);
            executorFast.invokeAll(deltfcnFastTasks);
        } catch (InterruptedException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void simulate(long numIterations) {
        super.simulate(numIterations);
        executorSlow.shutdown();
        executorFast.shutdown();
    }

    @Override
    public void simulate(double timeInterval) {
        super.simulate(timeInterval);
        executorSlow.shutdown();
        executorFast.shutdown();
    }

    public static void printUsage() {
        System.err.println("Usage: java -cp " + DevStoneParallelCoordinator.class.getCanonicalName() + " <model-path> <slow-threads> <fast-threads>");
        System.err.println("    - <model>: DEVStone XML model file");
        System.err.println("    - <slow-threads>: number of slow threads");
        System.err.println("    - <fast-threads>: number of fast threads");
    }

    public static void main(String[] args) {
        if(args.length!=3) {
            // model, slow-threads, fast-threads
            System.err.println("Invalid number of arguments.");
            printUsage();
            return;
        }
        DevsLogger.setup("ho15-chisquare2-s" + args[1] + "-f" + args[2] + ".log", Level.INFO);

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

        // Analizamos los atómicos que son slow o fast:
        HashSet<String> slowModels = new HashSet<>();
        HashSet<String> fastModels = new HashSet<>();
    
        NodeList xmlChildList = xmlCoupled.getChildNodes();
        for (int i = 0; i < xmlChildList.getLength(); ++i) {
            Node xmlNode = xmlChildList.item(i);
            Element xmlChild;
            String nodeName = xmlNode.getNodeName();
            if (nodeName.equals("atomic")) {
                xmlChild = (Element) xmlNode;
                String atomicName = xmlChild.getAttribute("name");
                String hostType = xmlChild.getAttribute("host");
                if (hostType.startsWith("slow")) {
                    LOGGER.fine("Adding model " + atomicName + " to the set of slow models.");
                    slowModels.add(atomicName);
                } else if (hostType.startsWith("fast")) {
                    LOGGER.fine("Adding model " + atomicName + " to the set of fast models.");
                    fastModels.add(atomicName);
                } else {
                    LOGGER.severe("ERROR. Bad host type:" + hostType + ". It must begin with slow or fast.");
                    return;
                }
            }
        }        

        LOGGER.fine("Creating coordinator ...");
        DevStoneParallelCoordinator coordinator = new DevStoneParallelCoordinator(model, Integer.parseInt(args[1]), slowModels, Integer.parseInt(args[2]), fastModels);
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