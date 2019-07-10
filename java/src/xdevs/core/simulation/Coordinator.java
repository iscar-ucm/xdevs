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
import java.util.LinkedList;
import java.util.logging.Logger;

import xdevs.core.util.Constants;
import xdevs.core.modeling.Coupling;
import xdevs.core.modeling.Port;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.util.Util;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coordinator extends AbstractSimulator {

    private static final Logger LOGGER = Logger.getLogger(Coordinator.class.getName());

    protected Coupled model;
    protected LinkedList<AbstractSimulator> simulators = new LinkedList<>();
    int totalIterations = 0;

    public Coordinator(SimulationClock clock, Coupled model, boolean flatten) {
        super(clock);
        if (flatten) {
            this.model = model.flatten();
        } else {
            this.model = model;
        }
        LOGGER.fine(model.getName() + "'s hierarchical...\n" + Util.printCouplings(model));
    }
    
    public Coordinator(SimulationClock clock, Coupled model) {
        this(clock, model, false);
    }

    public Coordinator(Coupled model, boolean flatten) {
        this(new SimulationClock(), model, flatten);
    }

    public Coordinator(Coupled model) {
        this(model, true);
    }
    
    protected void buildHierarchy() {
          // Build hierarchy
        Collection<Component> components = model.getComponents();
        for (Component component : components) {
            if (component instanceof Coupled) {
                Coordinator coordinator = new Coordinator(clock, (Coupled) component, false);
                simulators.add(coordinator);
            } else if (component instanceof Atomic) {
                Simulator simulator = new Simulator(clock, (Atomic) component);
                simulators.add(simulator);
            }
        }
    }

    @Override
    public void initialize() {
        this.buildHierarchy();
        for (AbstractSimulator simulator : simulators) {
            simulator.initialize();
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void exit() {
        for (AbstractSimulator simulator : simulators) {
            simulator.exit();
        }
    }

    public Collection<AbstractSimulator> getSimulators() {
        return simulators;
    }

    @Override
    public double ta() {
        double tn = Constants.INFINITY;
        for (AbstractSimulator simulator : simulators) {
            if (simulator.getTN() < tn) {
                tn = simulator.getTN();
            }
        }
        return tn - clock.getTime();
    }

    @Override
    public void lambda() {
        for (AbstractSimulator simulator : simulators) {
            simulator.lambda();
        }
        propagateOutput();
    }

    public void propagateOutput() {
        LinkedList<Coupling<?>> ic = model.getIC();
        for (Coupling<?> c : ic) {
            c.propagateValues();
        }

        LinkedList<Coupling<?>> eoc = model.getEOC();
        for (Coupling<?> c : eoc) {
            c.propagateValues();
        }
    }

    @Override
    public void deltfcn() {
        propagateInput();
        for (AbstractSimulator simulator : simulators) {
            simulator.deltfcn();
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    public void propagateInput() {
        LinkedList<Coupling<?>> eic = model.getEIC();
        for (Coupling<?> c : eic) {
            c.propagateValues();
        }
    }

    @Override
    public void clear() {
        for (AbstractSimulator simulator : simulators) {
            simulator.clear();
        }
        Collection<Port<?>> inPorts;
        inPorts = model.getInPorts();
        for (Port<?> port : inPorts) {
            port.clear();
        }
        Collection<Port<?>> outPorts;
        outPorts = model.getOutPorts();
        for (Port<?> port : outPorts) {
            port.clear();
        }
    }

    /**
     * Injects a value into the port "port", calling the transition function.
     *
     * @param e elapsed time
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(double e, Port port, Collection<Object> values) {
        double time = clock.getTime() + e;
        if (time <= tN) {
            port.addValues(values);
            clock.setTime(time);
            deltfcn();
            clear();
        } else {
            LOGGER.severe("Time: " + tL + " - ERROR input rejected: elapsed time " + e + " is not in bounds.");
        }
    }

    /**
     * @see xdevs.core.simulation.Coordinator#simInject(double, xdevs.core.modeling.Port, java.util.Collection) 
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(Port port, Collection<Object> values) {
        simInject(0.0, port, values);
    }

    /**
     * Injects a single value in the given input port with elapsed time e.
     *
     * @see xdevs.core.simulation.Coordinator#simInject(double, xdevs.core.modeling.Port, java.util.Collection) 
     * @param e
     * @param port
     * @param value
     */
    public void simInject(double e, Port port, Object value) {
        LinkedList values = new LinkedList();
        values.add(value);
        simInject(e, port, values);
    }

    /**
     * Injects a single value in the given input port with elapsed time e equal
     * to 0.
     *
     * @see xdevs.core.simulation.Coordinator#simInject(double, xdevs.core.modeling.Port, java.util.Collection) 
     * @param port
     * @param value
     */
    public void simInject(Port port, Object value) {
        simInject(0.0, port, value);
    }

    public void simulate(long numIterations) {
        LOGGER.fine("START SIMULATION");
        totalIterations += numIterations;
        long counter;
        for (counter = 1; counter < totalIterations
                && tN < Constants.INFINITY; counter++) {
            clock.setTime(tN);
            lambda();
            deltfcn();
            clear();
        }
    }

    public void simulate(double timeInterval) {
        LOGGER.fine("START SIMULATION");
        //clock.setTime(tN);
        double tF = clock.getTime() + timeInterval;
        while (clock.getTime() < Constants.INFINITY && tN < tF) {
            clock.setTime(tN);
            lambda();
            deltfcn();
            clear();
        }

        clock.setTime(tF);
    }

    @Override
    public Coupled getModel() {
        return model;
    }

}
