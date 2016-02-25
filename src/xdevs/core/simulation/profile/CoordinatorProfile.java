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
import java.util.LinkedList;
import java.util.logging.Level;

import xdevs.core.modeling.InPort;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.api.CoordinatorInterface;
import xdevs.core.simulation.api.SimulatorInterface;
import xdevs.core.test.efp.Efp;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author José Luis Risco Martín
 */
public class CoordinatorProfile extends SimulatorProfile implements CoordinatorInterface {

    //private static final Logger logger = Logger.getLogger(CoordinatorProfile.class.getName());
    protected CoordinatorInterface realCoordinator;

    protected long numCallsToGetSimulators = 0;
    protected long timeUsedByGetSimulators = 0;
    protected long numCallsToPropagateOutput = 0;
    protected long timeUsedByPropagateOutput = 0;
    protected long numCallsToPropagateInput = 0;
    protected long timeUsedByPropagateInput = 0;
    protected double executionTime = 0.0;

    public CoordinatorProfile(CoordinatorInterface realCoordinator) {
        super(realCoordinator);
        this.realCoordinator = realCoordinator;

        Collection<SimulatorInterface> realSimulators = realCoordinator.getSimulators();
        Collection<SimulatorInterface> profSimulators = new LinkedList<>();
        for (SimulatorInterface realSimulatorAux : realSimulators) {
            if (realSimulatorAux instanceof CoordinatorInterface) {
                CoordinatorProfile profCoordinator = new CoordinatorProfile((CoordinatorInterface) realSimulatorAux);
                profSimulators.add(profCoordinator);
            } else if (realSimulatorAux instanceof SimulatorInterface) {
                SimulatorProfile profSimulator = new SimulatorProfile(realSimulatorAux);
                profSimulators.add(profSimulator);
            }
        }
        realSimulators.clear();
        realSimulators.addAll(profSimulators);
    }

    @Override
    public Collection<SimulatorInterface> getSimulators() {
        this.numCallsToGetSimulators++;
        long start = System.currentTimeMillis();
        Collection<SimulatorInterface> result = realCoordinator.getSimulators();
        long end = System.currentTimeMillis();
        this.timeUsedByGetSimulators += (end - start);
        return result;
    }

    @Override
    public void propagateOutput() {
        this.numCallsToPropagateOutput++;
        long start = System.currentTimeMillis();
        realCoordinator.propagateOutput();
        long end = System.currentTimeMillis();
        this.timeUsedByPropagateOutput += (end - start);
    }

    @Override
    public void propagateInput() {
        this.numCallsToPropagateInput++;
        long start = System.currentTimeMillis();
        realCoordinator.propagateInput();
        long end = System.currentTimeMillis();
        this.timeUsedByPropagateInput += (end - start);
    }

    @Override
    public void simulate(long numIterations) {
        long start = System.currentTimeMillis();
        realCoordinator.simulate(numIterations);
        long end = System.currentTimeMillis();
        this.executionTime += (end - start);
    }

    @Override
    public void simulate(double timeInterval) {
        long start = System.currentTimeMillis();
        realCoordinator.simulate(timeInterval);
        long end = System.currentTimeMillis();
        this.executionTime += (end - start);
    }

    @Override
    public String getStats() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("========================================================================\n");
        buffer.append("Statistics for ").append(realCoordinator.getModel().getName()).append(":\n");
        buffer.append("========================================================================\n");
        buffer.append(super.getStats());
        buffer.append("numCallsToGetSimulators = ").append(numCallsToGetSimulators).append("\n");
        buffer.append("timeUsedByGetSimulators = ").append(timeUsedByGetSimulators).append(" ms\n");
        buffer.append("numCallsToPropagateOutput = ").append(numCallsToPropagateOutput).append("\n");
        buffer.append("timeUsedByPropagateOutput = ").append(timeUsedByPropagateOutput).append(" ms\n");
        buffer.append("numCallsToPropagateInput = ").append(numCallsToPropagateInput).append("\n");
        buffer.append("timeUsedByPropagateInput = ").append(timeUsedByPropagateInput).append(" ms\n");
        buffer.append("executionTime = ").append(executionTime).append(" ms\n");
        for (SimulatorInterface simulator : realCoordinator.getSimulators()) {
            buffer.append(((SimulatorProfile) simulator).getStats());
        }
        return buffer.toString();
    }

    public static void main(String args[]) {
        DevsLogger.setup(Level.INFO);
        Efp efp = new Efp("efp", 1, 3, 100);
        CoordinatorProfile coordinator = new CoordinatorProfile(new Coordinator(efp, false));
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        System.out.println(coordinator.getStats());
    }

    @Override
    public void simInject(double e, InPort port, Collection<Object> values) {
        realCoordinator.simInject(e, port, values);
    }

    @Override
    public void simInject(InPort port, Collection<Object> values) {
        realCoordinator.simInject(port, values);
    }

    @Override
    public void simInject(double e, InPort port, Object value) {
        realCoordinator.simInject(e, port, value);
    }

    @Override
    public void simInject(InPort port, Object value) {
        realCoordinator.simInject(port, value);
    }

}
