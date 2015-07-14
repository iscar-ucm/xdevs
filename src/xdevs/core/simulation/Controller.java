package xdevs.core.simulation;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.Constants;
import xdevs.core.modeling.InPort;
import xdevs.core.simulation.api.ControllerInterface;
import xdevs.core.simulation.api.CoordinatorInterface;
import xdevs.core.simulation.api.SimulationClock;

/**
 *
 * @author José Luis Risco Martín
 */
public class Controller extends Thread implements ControllerInterface {

    protected CoordinatorInterface coordinator;
    private boolean suspended = false;
    protected double tF = Constants.INFINITY;

    public Controller(CoordinatorInterface coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void startSimulation(double timeInterval) {
        coordinator.initialize();
        coordinator.getClock().setTime(coordinator.getTN());
        tF = coordinator.getClock().getTime() + timeInterval;
    }

    @Override
    public void runSimulation() {
        super.start();
    }

    @Override
    public void pauseSimulation() {
        suspended = true;
    }

    @Override
    public void resumeSimulation() {
        suspended = false;
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void stepSimulation() {
        if (coordinator.getClock().getTime() < tF) {
            coordinator.lambda();
            coordinator.deltfcn();
            coordinator.clear();
            coordinator.getClock().setTime(coordinator.getTN());
        }
    }

    @Override
    public void terminateSimulation() {
        super.interrupt();
    }

    @Override
    public void simInject(double e, InPort port, Collection<Object> values) {
        coordinator.simInject(e, port, values);
    }

    @Override
    public void simInject(InPort port, Collection<Object> values) {
        coordinator.simInject(port, values);
    }

    @Override
    public void simInject(double e, InPort port, Object value) {
        coordinator.simInject(e, port, value);
    }

    @Override
    public void simInject(InPort port, Object value) {
        coordinator.simInject(port, value);
    }

    @Override
    public SimulationClock getSimulationClock() {
        return coordinator.getClock();
    }

    @Override
    public void run() {
        try {
            while (!super.isInterrupted() && coordinator.getClock().getTime() < Constants.INFINITY && coordinator.getClock().getTime() < tF) {
                synchronized (this) {
                    while (suspended) {
                        wait();
                    }
                }
                coordinator.lambda();
                coordinator.deltfcn();
                coordinator.clear();
                coordinator.getClock().setTime(coordinator.getTN());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
