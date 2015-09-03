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
package xdevs.core.simulation.api;

import java.util.Collection;
import xdevs.core.modeling.InPort;

/**
 * This controller has been created to control the simulation in GUI-based environments
 * @author José Luis Risco Martín
 */
public interface ControllerInterface {

    /**
     * Initializes the Coordinator.
     *
     * @see mitris.sim.core.simulation.api.CoordinatorInterface#initialize()
     */
    // public void initialize();

    /**
     * Sets the current Coordinator to coordinator
     * @param coordinator the new Coordinator
     */
    // public void selectCoordinator(CoordinatorInterface coordinator);

    /**
     * Starts the simulation, initializing the coordinator
     * @param timeInterval Simulation time interval, in seconds
     */
    public void startSimulation(double timeInterval);

    /**
     * Runs the simulation during a given time interval
     * @see #startSimulation(double) 
     */
    public void runSimulation();

    /**
     * Pauses the simulation
     */
    public void pauseSimulation();
    
    /**
     * Resumes the simulation
     */
    public void resumeSimulation();

    /**
     * Performs a single step in the simulation loop
     */
    public void stepSimulation();

    /**
     * Finishes the current simulation
     */
    public void terminateSimulation();
    
    /**
     * Injects a value into the port "port", calling the transition function.
     * @param e Elapsed time
     * @param port Input port
     * @param values Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) 
     */
    public void simInject(double e, InPort port, Collection<Object> values);

    /**
     * Injects a value into the port "port", calling the transition function.
     * @param port Input port
     * @param values Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(mitris.sim.core.modeling.InPort, java.util.Collection) 
     */
    public void simInject(InPort port, Collection<Object> values);

    /**
     * Injects a single value into the port "port", calling the transition function.
     * @param e Elapsed time
     * @param port Input port
     * @param value Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.lang.Object) 
     */
    public void simInject(double e, InPort port, Object value);

    /**
     * Injects a single value into the port "port", calling the transition function.
     * @param port Input port
     * @param value Set of values
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(mitris.sim.core.modeling.InPort, java.lang.Object) 
     */
    public void simInject(InPort port, Object value);

    /**
     * Returns the simulation clock.
     * @return the simulation clock
     */
    public SimulationClock getSimulationClock();
}
