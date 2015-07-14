package xdevs.core.simulation.parallel;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.lib.examples.Efp;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.api.SimulatorInterface;
import xdevs.core.simulation.api.SimulationClock;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author jlrisco
 */
public class CoordinatorParallel extends Coordinator {

    private static final Logger logger = Logger.getLogger(CoordinatorParallel.class.getName());

    protected int numberOfThreads;
    protected LinkedList<TaskLambda> lambdaTasks = new LinkedList<>();
    protected LinkedList<TaskDeltFcn> deltfcnTasks = new LinkedList<>();
    protected ExecutorService executor;

    public CoordinatorParallel(SimulationClock clock, Coupled model, int numberOfThreads) {
        super(clock, model, true);
        this.numberOfThreads = numberOfThreads;
        executor = Executors.newFixedThreadPool(numberOfThreads);
        for (SimulatorInterface simulator : simulators) {
            lambdaTasks.add(new TaskLambda(simulator));
        }
        for (SimulatorInterface simulator : simulators) {
            deltfcnTasks.add(new TaskDeltFcn(simulator));
        }
    }

    public CoordinatorParallel(SimulationClock clock, Coupled model) {
        this(clock, model, Runtime.getRuntime().availableProcessors());
    }

    public CoordinatorParallel(Coupled model) {
        this(new SimulationClock(), model, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void lambda() {
        try {
            executor.invokeAll(lambdaTasks);
        } catch (InterruptedException ee) {
            logger.severe(ee.getLocalizedMessage());
        }
        propagateOutput();
    }

    @Override
    public void deltfcn() {
        propagateInput();
        try {
            executor.invokeAll(deltfcnTasks);
        } catch (InterruptedException ee) {
            logger.severe(ee.getLocalizedMessage());
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void simulate(long numIterations) {
        super.simulate(numIterations);
        executor.shutdown();
    }

    @Override
    public void simulate(double timeInterval) {
        super.simulate(timeInterval);
        executor.shutdown();
    }

    public static void main(String[] args) {
        DevsLogger.setup(Level.FINE);
        Efp efp = new Efp("EFP", 1, 3, 30);
        CoordinatorParallel coordinator = new CoordinatorParallel(efp);
        coordinator.initialize();
        coordinator.simulate(60.0);
    }
}
