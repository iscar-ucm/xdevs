package xdevs.lib.tmp_journal;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.parallel.TaskDeltFcn;
import xdevs.core.simulation.parallel.TaskLambda;

/**
 *
 * @author jlrisco
 */
public class DevStoneParallel extends Coordinator {

    private static final Logger LOGGER = Logger.getLogger(DevStoneParallel.class.getName());

    protected int numberOfThreads;
    protected LinkedList<TaskLambda> lambdaSlowTasks = new LinkedList<>();
    protected LinkedList<TaskDeltFcn> deltfcnSlowTasks = new LinkedList<>();
    protected LinkedList<TaskLambda> lambdaFastTasks = new LinkedList<>();
    protected LinkedList<TaskDeltFcn> deltfcnFastTasks = new LinkedList<>();
    protected ExecutorService executorSlow;
    protected ExecutorService executorFast;

    public DevStoneParallel(Coupled model, int numberOfSlowThreads, int numberOfFastThreads) {
        super(new SimulationClock(), model, true);
        this.numberOfThreads = numberOfSlowThreads+numberOfFastThreads;
        executorSlow = Executors.newFixedThreadPool(numberOfSlowThreads);
        executorFast = Executors.newFixedThreadPool(numberOfFastThreads);
    }

    @Override
    public void buildHierarchy() {
        super.buildHierarchy();
        simulators.forEach(simulator -> {
            Component component = simulator.getModel();
            if (component.getClass().getSimpleName().equals(StaticSensorPayload.class.getSimpleName())) {
                lambdaTasksSensorModel.add(new TaskLambda(simulator));
                deltfcnTasksSensorModel.add(new TaskDeltFcn(simulator));                
            }
            else {
                lambdaTasksRest.add(new TaskLambda(simulator));
                deltfcnTasksRest.add(new TaskDeltFcn(simulator));
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

    public static void main(String[] args) {
        // ...
    }
}