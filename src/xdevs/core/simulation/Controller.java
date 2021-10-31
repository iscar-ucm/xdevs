/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín <jlrisco@ucm.es>
 *  - Saurabh Mittal <smittal@duniptech.com>
 */
package xdevs.core.simulation;

import java.util.Collection;
import java.util.logging.Logger;
import xdevs.core.modeling.Port;
import xdevs.core.util.Constants;

/**
 * This controller has been created to control the simulation in GUI-based environments
 * @author José Luis Risco Martín
 */
public class Controller extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    protected Coordinator coordinator;
    private boolean suspended = false;
    protected double tF = Constants.INFINITY;

    public Controller(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

 /**
     * Starts the simulation, initializing the coordinator
     * @param timeInterval Simulation time interval, in seconds
     */
    public void startSimulation(double timeInterval) {
        coordinator.initialize();
        coordinator.getClock().setTime(coordinator.getTN());
        tF = coordinator.getClock().getTime() + timeInterval;
    }

    /**
     * Runs the simulation during a given time interval
     * @see #startSimulation(double) 
     */
    public void runSimulation() {
        super.start();
    }

    /**
     * Pauses the simulation
     */
    public void pauseSimulation() {
        suspended = true;
    }

    /**
     * Resumes the simulation
     */
    public void resumeSimulation() {
        suspended = false;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Performs a single step in the simulation loop
     */
    public void stepSimulation() {
        if (coordinator.getClock().getTime() < tF) {
            coordinator.lambda();
            coordinator.deltfcn();
            coordinator.clear();
            coordinator.getClock().setTime(coordinator.getTN());
        }
    }

     /**
     * Finishes the current simulation
     */    
    public void terminateSimulation() {
        super.interrupt();
    }

     /**
     * Injects a value into the port "port", calling the transition function.
     * @param e Elapsed time
     * @param port Input port
     * @param values Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) 
     */
    public void simInject(double e, Port<Object> port, Collection<Object> values) {
        coordinator.simInject(e, port, values);
    }

     /**
     * Injects a value into the port "port", calling the transition function.
     * @param port Input port
     * @param values Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(mitris.sim.core.modeling.InPort, java.util.Collection) 
     */
    public void simInject(Port<Object> port, Collection<Object> values) {
        coordinator.simInject(port, values);
    }

    /**
     * Injects a single value into the port "port", calling the transition function.
     * @param e Elapsed time
     * @param port Input port
     * @param value Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.lang.Object) 
     */
    public void simInject(double e, Port<Object> port, Object value) {
        coordinator.simInject(e, port, value);
    }

    /**
     * Injects a single value into the port "port", calling the transition function.
     * @param port Input port
     * @param value Set of values
     * @see xdevs.core.simulation.Coordinator#simInject(xdevs.core.modeling.Port, java.lang.Object)
     */
    public void simInject(Port<Object> port, Object value) {
        coordinator.simInject(port, value);
    }

     /**
     * Returns the simulation clock.
     * @return the simulation clock
     */
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
            LOGGER.severe(ex.getLocalizedMessage());
        }
    }
}
