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
import java.util.logging.Level;
import java.util.logging.Logger;

import xdevs.core.examples.efp.Efp;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Coupling;
import xdevs.core.modeling.Port;
import xdevs.core.util.Constants;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coordinator extends AbstractSimulator {

    private static final Logger LOGGER = Logger.getLogger(Coordinator.class.getName());

    protected Coupled model;
    protected LinkedList<AbstractSimulator> simulators = new LinkedList<>();
    long totalIterations = 0, countIterations = 0;

    public Coordinator(SimulationClock clock, Coupled model, boolean flatten) {
        super(clock);
        if (flatten) {
            this.model = model.flatten();
        } else {
            this.model = model;
        }
    }
    
    public Coordinator(SimulationClock clock, Coupled model) {
        this(clock, model, false);
    }

    public Coordinator(Coupled model, boolean flatten) {
        this(new SimulationClock(), model, flatten);
    }

    public Coordinator(Coupled model) {
        this(model, false);
    }
    
    protected void buildHierarchy() {
          // Build hierarchy
        Collection<Component> components = model.getComponents();
        components.forEach((component) -> {
            if (component instanceof Coupled) {
                Coordinator coordinator = new Coordinator(clock, (Coupled) component, false);
                simulators.add(coordinator);
            } else if (component instanceof Atomic) {
                Simulator simulator = new Simulator(clock, (Atomic) component);
                simulators.add(simulator);
            }
        });
    }

    @Override
    public void initialize() {
        this.buildHierarchy();
        simulators.forEach((simulator) -> {
            simulator.initialize();
        });
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void exit() {
        simulators.forEach((simulator) -> {
            simulator.exit();
        });
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
        simulators.forEach((simulator) -> {
            simulator.lambda();
        });
        propagateOutput();
    }

    public void propagateOutput() {
        LinkedList<Coupling<?>> ic = model.getIC();
        ic.forEach((c) -> {
            c.propagateValues();
        });

        LinkedList<Coupling<?>> eoc = model.getEOC();
        eoc.forEach((c) -> {
            c.propagateValues();
        });
    }

    @Override
    public void deltfcn() {
        propagateInput();
        simulators.forEach((simulator) -> {
            simulator.deltfcn();
        });
        tL = clock.getTime();
        tN = tL + ta();
    }

    public void propagateInput() {
        LinkedList<Coupling<?>> eic = model.getEIC();
        eic.forEach((c) -> {
            c.propagateValues();
        });
    }

    @Override
    public void clear() {
        simulators.forEach((simulator) -> {
            simulator.clear();
        });
        Collection<Port<?>> inPorts;
        inPorts = model.getInPorts();
        inPorts.forEach((port) -> {
            port.clear();
        });
        Collection<Port<?>> outPorts;
        outPorts = model.getOutPorts();
        outPorts.forEach((port) -> {
            port.clear();
        });
    }

    /**
     * Injects a value into the port "port", calling the transition function.
     *
     * @param e elapsed time
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(double e, Port<Object> port, Collection<Object> values) {
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
    public void simInject(Port<Object> port, Collection<Object> values) {
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
    public void simInject(double e, Port<Object> port, Object value) {
        LinkedList<Object> values = new LinkedList<>();
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
    public void simInject(Port<Object> port, Object value) {
        simInject(0.0, port, value);
    }

    public void simulate(long numIterations) {
        LOGGER.fine("START SIMULATION");
        totalIterations += numIterations;
        while (countIterations < totalIterations && tN < Constants.INFINITY) {
            clock.setTime(tN);
            lambda();
            deltfcn();
            clear();
            countIterations++;
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

    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        Efp efp = new Efp("EFP", 1, 3, 1000000);
        Coordinator coordinator = new Coordinator(efp);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}
