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

import xdevs.core.modeling.Atomic;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.Simulator;

/**
 *
 * @author José Luis Risco Martín
 */
public class SimulatorProfile extends Simulator {

    // AbstractSimulatorCalls
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

    public SimulatorProfile(SimulationClock clock, Atomic model) {
        super(clock, model);
    }

    // AbstractSimulatorCalls
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
    public void deltfcn() {
        this.numCallsToDeltFcn++;
        long start = System.currentTimeMillis();
        super.deltfcn();
        long end = System.currentTimeMillis();
        this.timeUsedByDeltFcn += (end - start);
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
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.getModel().getName()).append(";");
        buffer.append(super.getModel().getClass().getSimpleName()).append(";");
        buffer.append("Atomic;");
        buffer.append(numCallsToTa).append(";");
        buffer.append(timeUsedByTa / 1000.0).append(";");
        buffer.append(numCallsToDeltFcn).append(";");
        buffer.append(timeUsedByDeltFcn / 1000.0).append(";");
        buffer.append(numCallsToLambda).append(";");
        buffer.append(timeUsedByLambda / 1000.0).append(";");
        buffer.append(numCallsToClear).append(";");
        buffer.append(timeUsedByClear / 1000.0).append(";");
        buffer.append(numCallsToInitialize).append(";");
        buffer.append(timeUsedByInitialize / 1000.0).append(";");
        buffer.append(numCallsToGetTN).append(";");
        buffer.append(timeUsedByGetTN / 1000.0).append(";");
        buffer.append(numCallsToGetTL).append(";");
        buffer.append(timeUsedByGetTL / 1000.0).append(";");
        buffer.append(numCallsToSetTN).append(";");
        buffer.append(timeUsedBySetTN / 1000.0).append(";");
        buffer.append(numCallsToSetTL).append(";");
        buffer.append(timeUsedBySetTL / 1000.0).append(";");
        buffer.append(numCallsToGetClock).append(";");
        buffer.append(timeUsedByGetClock / 1000.0).append(";");
        buffer.append("0;0;0;0;0;0;0\n");
        return buffer.toString();
    }

}
