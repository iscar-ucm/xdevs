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
package xdevs.core.simulation.profile;

import java.util.Collection;
import java.util.logging.Level;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.AbstractSimulator;

import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.test.efp.Efp;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author José Luis Risco Martín
 */
public class CoordinatorProfile extends Coordinator {

    // AbstractSimulator Calls
    protected long numCallsToInitialize = 0;
    protected long timeUsedByInitialize = 0;
    protected long numCallsToExit = 0;
    protected long timeUsedByExit = 0;
    protected long numCallsToTa = 0;
    protected long timeUsedByTa = 0;
    protected long numCallsToDeltFcn = 0;
    protected long timeUsedByDeltFcn = 0;
    protected long numCallsToLambda = 0;
    protected long timeUsedByLambda = 0;
    protected long numCallsToClear = 0;
    protected long timeUsedByClear = 0;
    protected long numCallsToGetTN = 0;
    protected long timeUsedByGetTN = 0;
    protected long numCallsToGetTL = 0;
    protected long timeUsedByGetTL = 0;
    protected long numCallsToSetTN = 0;
    protected long timeUsedBySetTN = 0;
    protected long numCallsToSetTL = 0;
    protected long timeUsedBySetTL = 0;
    protected long numCallsToGetClock = 0;
    protected long timeUsedByGetClock = 0;

    protected long numCallsToGetSimulators = 0;
    protected long timeUsedByGetSimulators = 0;
    protected long numCallsToPropagateOutput = 0;
    protected long timeUsedByPropagateOutput = 0;
    protected long numCallsToPropagateInput = 0;
    protected long timeUsedByPropagateInput = 0;
    protected double executionTime = 0.0;

    public CoordinatorProfile(SimulationClock clock, Coupled model) {
        super(clock, model);
    }

    public CoordinatorProfile(Coupled model) {
        super(model);
    }

    @Override
    protected void buildHierarchy() {
        // Build hierarchy
        Collection<Component> components = model.getComponents();
        components.forEach((component) -> {
            if (component instanceof Coupled) {
                CoordinatorProfile coordinator = new CoordinatorProfile(clock, (Coupled) component);
                simulators.add(coordinator);
            } else if (component instanceof Atomic) {
                SimulatorProfile simulator = new SimulatorProfile(clock, (Atomic) component);
                simulators.add(simulator);
            }
        });
    }

    // AbstractSimulator Calls
    @Override
    public void initialize() {
        this.numCallsToInitialize++;
        long start = System.currentTimeMillis();
        super.initialize();
        long end = System.currentTimeMillis();
        this.timeUsedByInitialize += (end - start);

    }

    @Override
    public void exit() {
        this.numCallsToExit++;
        long start = System.currentTimeMillis();
        super.exit();
        long end = System.currentTimeMillis();
        this.timeUsedByExit += (end - start);

    }

    @Override
    public double ta() {
        numCallsToTa++;
        long start = System.currentTimeMillis();
        double result = super.ta();
        long end = System.currentTimeMillis();
        timeUsedByTa += (end - start);
        return result;
    }

    @Override
    public void lambda() {
        this.numCallsToLambda++;
        long start = System.currentTimeMillis();
        super.lambda();
        long end = System.currentTimeMillis();
        this.timeUsedByLambda += (end - start);
    }

    @Override
    public void deltfcn() {
        this.numCallsToDeltFcn++;
        long start = System.currentTimeMillis();
        super.deltfcn();
        long end = System.currentTimeMillis();
        this.timeUsedByDeltFcn += (end - start);
    }

    @Override
    public void clear() {
        this.numCallsToClear++;
        long start = System.currentTimeMillis();
        super.clear();
        long end = System.currentTimeMillis();
        this.timeUsedByClear += (end - start);
    }
    
    @Override
    public double getTL() {
        this.numCallsToGetTL++;
        long start = System.currentTimeMillis();
        double result = super.getTL();
        long end = System.currentTimeMillis();
        this.timeUsedByGetTL += (end - start);
        return result;
    }
    
    @Override
    public void setTL(double tL) {
        this.numCallsToSetTL++;
        long start = System.currentTimeMillis();
        super.setTL(tL);
        long end = System.currentTimeMillis();
        this.timeUsedBySetTL += (end - start);
    }

    @Override
    public double getTN() {
        this.numCallsToGetTN++;
        long start = System.currentTimeMillis();
        double result = super.getTN();
        long end = System.currentTimeMillis();
        this.timeUsedByGetTN += (end - start);
        return result;
    }

    @Override
    public void setTN(double tN) {
        this.numCallsToSetTN++;
        long start = System.currentTimeMillis();
        super.setTN(tN);
        long end = System.currentTimeMillis();
        this.timeUsedBySetTN += (end - start);
    }
    
    @Override
    public SimulationClock getClock() {
        this.numCallsToGetClock++;
        long start = System.currentTimeMillis();
        SimulationClock result = super.getClock();
        long end = System.currentTimeMillis();
        this.timeUsedByGetClock += (end - start);
        return result;
    }
    
    @Override
    public Collection<AbstractSimulator> getSimulators() {
        this.numCallsToGetSimulators++;
        long start = System.currentTimeMillis();
        Collection<AbstractSimulator> result = super.getSimulators();
        long end = System.currentTimeMillis();
        this.timeUsedByGetSimulators += (end - start);
        return result;
    }

    @Override
    public void propagateOutput() {
        this.numCallsToPropagateOutput++;
        long start = System.currentTimeMillis();
        super.propagateOutput();
        long end = System.currentTimeMillis();
        this.timeUsedByPropagateOutput += (end - start);
    }

    @Override
    public void propagateInput() {
        this.numCallsToPropagateInput++;
        long start = System.currentTimeMillis();
        super.propagateInput();
        long end = System.currentTimeMillis();
        this.timeUsedByPropagateInput += (end - start);
    }

    @Override
    public void simulate(long numIterations) {
        long start = System.currentTimeMillis();
        super.simulate(numIterations);
        long end = System.currentTimeMillis();
        this.executionTime += (end - start);
    }

    @Override
    public void simulate(double timeInterval) {
        long start = System.currentTimeMillis();
        super.simulate(timeInterval);
        long end = System.currentTimeMillis();
        this.executionTime += (end - start);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("========================================================================\n");
        buffer.append("Statistics for ").append(super.getModel().getName()).append(":\n");
        buffer.append("========================================================================\n");
        buffer.append("numCallsToTa = ").append(numCallsToTa).append("\n");
        buffer.append("timeUsedByTa = ").append(timeUsedByTa).append(" ms\n");
        buffer.append("numCallsToDeltFcn = ").append(numCallsToDeltFcn).append("\n");
        buffer.append("timeUsedByDeltFcn = ").append(timeUsedByDeltFcn).append(" ms\n");
        buffer.append("numCallsToLambda = ").append(numCallsToLambda).append("\n");
        buffer.append("timeUsedByLambda = ").append(timeUsedByLambda).append(" ms\n");
        buffer.append("numCallsToClear = ").append(numCallsToClear).append("\n");
        buffer.append("timeUsedByClear = ").append(timeUsedByClear).append(" ms\n");
        buffer.append("numCallsToInitialize = ").append(numCallsToInitialize).append("\n");
        buffer.append("timeUsedByInitialize = ").append(timeUsedByInitialize).append(" ms\n");
        buffer.append("numCallsToGetTN = ").append(numCallsToGetTN).append("\n");
        buffer.append("timeUsedByGetTN = ").append(timeUsedByGetTN).append(" ms\n");
        buffer.append("numCallsToGetTL = ").append(numCallsToGetTL).append("\n");
        buffer.append("timeUsedByGetTL = ").append(timeUsedByGetTL).append(" ms\n");
        buffer.append("numCallsToSetTN = ").append(numCallsToSetTN).append("\n");
        buffer.append("timeUsedBySetTN = ").append(timeUsedBySetTN).append(" ms\n");
        buffer.append("numCallsToSetTL = ").append(numCallsToSetTL).append("\n");
        buffer.append("timeUsedBySetTL = ").append(timeUsedBySetTL).append(" ms\n");
        buffer.append("numCallsToGetClock = ").append(numCallsToGetClock).append("\n");
        buffer.append("timeUsedByGetClock = ").append(timeUsedByGetClock).append(" ms\n");
        buffer.append("numCallsToGetSimulators = ").append(numCallsToGetSimulators).append("\n");
        buffer.append("timeUsedByGetSimulators = ").append(timeUsedByGetSimulators).append(" ms\n");
        buffer.append("numCallsToPropagateOutput = ").append(numCallsToPropagateOutput).append("\n");
        buffer.append("timeUsedByPropagateOutput = ").append(timeUsedByPropagateOutput).append(" ms\n");
        buffer.append("numCallsToPropagateInput = ").append(numCallsToPropagateInput).append("\n");
        buffer.append("timeUsedByPropagateInput = ").append(timeUsedByPropagateInput).append(" ms\n");
        buffer.append("executionTime = ").append(executionTime).append(" ms\n");
        super.getSimulators().forEach((simulator) -> {
            buffer.append(simulator.toString());
        });
        return buffer.toString();
    }

    public static void main(String args[]) {
        DevsLogger.setup(Level.INFO);
        Efp efp = new Efp("efp", 1, 3, 100);
        CoordinatorProfile coordinator = new CoordinatorProfile(efp);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        System.out.println(coordinator.toString());
    }
}
