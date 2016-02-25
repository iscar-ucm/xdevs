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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import xdevs.core.util.Constants;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Coupling;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.modeling.api.ComponentInterface;
import xdevs.core.modeling.api.AtomicInterface;
import xdevs.core.modeling.api.CoupledInterface;
import xdevs.core.simulation.api.CoordinatorInterface;
import xdevs.core.simulation.api.SimulatorInterface;
import xdevs.core.simulation.api.SimulationClock;
import xdevs.core.util.Util;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coordinator extends AbstractSimulator implements CoordinatorInterface {

    private static final Logger logger = Logger.getLogger(Coordinator.class
            .getName());

    protected CoupledInterface model;
    protected LinkedList<SimulatorInterface> simulators = new LinkedList<>();

    public Coordinator(SimulationClock clock, Coupled model, boolean flatten) {
        super(clock);
        logger.fine(model.getName() + "'s hierarchical...\n" + Util.printCouplings(model));
        if (flatten) {
            this.model = model.flatten();
        } else {
            this.model = model;
        }
        // Build hierarchy
        Collection<ComponentInterface> components = model.getComponents();
        for (ComponentInterface component : components) {
            if (component instanceof Coupled) {
                Coordinator coordinator = new Coordinator(clock,
                        (Coupled) component, false);
                simulators.add(coordinator);
            } else if (component instanceof AtomicInterface) {
                Simulator simulator = new Simulator(clock,
                        (AtomicInterface) component);
                simulators.add(simulator);
            }
        }

        if (flatten) {
            logger.fine("After flattening.....\n" + Util.printCouplings(this.model));
        }
        logger.fine(this.model.toString());
        Iterator<ComponentInterface> itr = this.model.getComponents().iterator();
        while (itr.hasNext()) {
            logger.fine("Component: " + itr.next());
        }
    }

    public Coordinator(Coupled model, boolean flatten) {
        this(new SimulationClock(), model, flatten);
    }

    public Coordinator(Coupled model) {
        this(model, true);
    }

    @Override
    public void initialize() {
        for (SimulatorInterface simulator : simulators) {
            simulator.initialize();
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void exit() {
        for (SimulatorInterface simulator : simulators) {
            simulator.exit();
        }
    }

    @Override
    public Collection<SimulatorInterface> getSimulators() {
        return simulators;
    }

    @Override
    public final double ta() {
        double tn = Constants.INFINITY;
        for (SimulatorInterface simulator : simulators) {
            if (simulator.getTN() < tn) {
                tn = simulator.getTN();
            }
        }
        return tn - clock.getTime();
    }

    @Override
    public void lambda() {
        for (SimulatorInterface simulator : simulators) {
            simulator.lambda();
        }
        propagateOutput();
    }

    @Override
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
        for (SimulatorInterface simulator : simulators) {
            simulator.deltfcn();
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    @Override
    public void propagateInput() {
        LinkedList<Coupling<?>> eic = model.getEIC();
        for (Coupling<?> c : eic) {
            c.propagateValues();
        }
    }

    @Override
    public void clear() {
        for (SimulatorInterface simulator : simulators) {
            simulator.clear();
        }
        Collection<InPort<?>> inPorts;
        inPorts = model.getInPorts();
        for (InPort<?> port : inPorts) {
            port.clear();
        }
        Collection<OutPort<?>> outPorts;
        outPorts = model.getOutPorts();
        for (OutPort<?> port : outPorts) {
            port.clear();
        }
    }

    @Override
    public void simInject(double e, InPort port, Collection<Object> values) {
        double time = tL + e;
        if (time <= tN) {
            port.addValues(values);
            clock.setTime(time);
            deltfcn();
        } else {
            logger.severe("Time: " + tL + " - ERROR input rejected: elapsed time " + e + " is not in bounds.");
        }
    }

    @Override
    public void simInject(InPort port, Collection<Object> values) {
        simInject(0.0, port, values);
    }

    @Override
    public void simInject(double e, InPort port, Object value) {
        LinkedList values = new LinkedList();
        values.add(value);
        simInject(e, port, values);
    }

    @Override
    public void simInject(InPort port, Object value) {
        simInject(0.0, port, value);
    }

    @Override
    public void simulate(long numIterations) {
        logger.fine("START SIMULATION");
        clock.setTime(tN);
        long counter;
        for (counter = 1; counter < numIterations
                && clock.getTime() < Constants.INFINITY; counter++) {
            lambda();
            deltfcn();
            clear();
            clock.setTime(tN);
        }
    }

    @Override
    public void simulate(double timeInterval) {
        logger.fine("START SIMULATION");
        clock.setTime(tN);
        double tF = clock.getTime() + timeInterval;
        while (clock.getTime() < Constants.INFINITY && clock.getTime() < tF) {
            lambda();
            deltfcn();
            clear();
            clock.setTime(tN);
        }
    }

    @Override
    public CoupledInterface getModel() {
        return model;
    }

}
