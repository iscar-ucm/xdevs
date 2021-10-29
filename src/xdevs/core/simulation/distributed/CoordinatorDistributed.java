/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.distributed.CoupledDistributed;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.util.Constants;

/**
 *
 * @author Almendras
 */
public class CoordinatorDistributed extends Coordinator {

    // CoordinatorDistributed constants
    private static final Logger LOGGER = Logger.getLogger(CoordinatorDistributed.class.getName());

    // CoordinatorDistributed attributes
    private ExecutorService executor;

    // CoordinatorDistributed Constructors
    public CoordinatorDistributed(SimulationClock clock, CoupledDistributed model) {
        super(clock, model);
        this.executor = Executors.newFixedThreadPool(model.getComponents().size());
        System.out.println("I am: " + this.model.getName());
        System.out.println("Workers: " + this.model.getComponents().toString());
    }

    public CoordinatorDistributed(CoupledDistributed model) {
        this(new SimulationClock(), model);
    }

    public LinkedList<DistributedTask> executeTasksList(int command) {
        LinkedList<DistributedTask> distributedTasks = new LinkedList<>();
        //System.out.println(System.currentTimeMillis() + ": Sending task " + command + "[c:" + String.valueOf(clock.getTime()) + "]");
        model.getComponents().forEach(component -> {
            String host = ((CoupledDistributed) model).getHost(component.getName());
            Integer port = ((CoupledDistributed) model).getMainPort(component.getName());
            distributedTasks.add(new DistributedTask(host, port, command, String.valueOf(clock.getTime())));
        });

        return distributedTasks;
    }

    @Override
    public void initialize() {
        try {
            executor.invokeAll(executeTasksList(Commands.INITIALIZE));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void exit() {
        MessageDistributed md;
        PingMessage pm;
        for (Component component : model.getComponents()) {
            String host = ((CoupledDistributed) model).getHost(component.getName());
            Integer mainPort = ((CoupledDistributed) model).getMainPort(component.getName());
            Integer auxPort = ((CoupledDistributed) model).getAuxPort(component.getName());
            md = new MessageDistributed(Commands.EXIT, String.valueOf(clock.getTime()));
            pm = new PingMessage(md, host, mainPort);
            pm.ping();
//            md = new MessageDistributed(Commands.EXIT_AUX, String.valueOf(clock.getTime()));
//            pm = new PingMessage(md, host, auxPort);
//            pm.ping();
        }
        executor.shutdown();
    }

    @Override
    public double ta() {
        double tn = Constants.INFINITY;
        try {
            List<Future<String>> tas = executor.invokeAll(executeTasksList(Commands.TA));
            for (Future<String> ta : tas) {
                if (Double.valueOf(ta.get()) < tn) {
                    tn = Double.valueOf(ta.get()); // simulator.getTN();
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
        return tn - clock.getTime();
    }

    @Override
    public void lambda() {
        try {
            executor.invokeAll(executeTasksList(Commands.LAMBDA));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
        propagateOutput();
    }

    @Override
    public void propagateOutput() {
        try {
            executor.invokeAll(executeTasksList(Commands.PROPAGATE_OUTPUT));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

    @Override
    public void deltfcn() {
        try {
            executor.invokeAll(executeTasksList(Commands.DELTFCN));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void clear() {
        try {
            executor.invokeAll(executeTasksList(Commands.CLEAR));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
    }

}
