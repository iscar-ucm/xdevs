package mitris.sim.core.simulation;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import mitris.sim.core.modeling.Coupled;

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
        super(clock, model.flatten());
        this.numberOfThreads = numberOfThreads;
        executor = Executors.newFixedThreadPool(numberOfThreads);
        for (AbstractSimulator simulator : simulators) {
            lambdaTasks.add(new TaskLambda(simulator));
        }
        for (AbstractSimulator simulator : simulators) {
            deltfcnTasks.add(new TaskDeltFcn(simulator));
        }
    }

    public CoordinatorParallel(SimulationClock clock, Coupled model) {
        this(clock, model.flatten(), Runtime.getRuntime().availableProcessors());
    }
    
    public CoordinatorParallel(Coupled model) {
        this(new SimulationClock(), model.flatten(), Runtime.getRuntime().availableProcessors());
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
    public void finalize() throws Throwable {
        try {
            executor.shutdown();
        } finally {
            super.finalize();
        }
    }
}
