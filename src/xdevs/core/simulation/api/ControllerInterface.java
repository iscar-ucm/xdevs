/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
