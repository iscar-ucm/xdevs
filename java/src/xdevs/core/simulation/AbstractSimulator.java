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

import xdevs.core.modeling.Component;

/**
 *
 * @author jlrisco
 */
public abstract class AbstractSimulator {

    protected SimulationClock clock;
    protected double tL; // Time of last event
    protected double tN; // Time of next event

    public AbstractSimulator(SimulationClock clock) {
        this.clock = clock;
    }

    public abstract void initialize();

    public abstract void exit();

    public abstract double ta();

    public abstract void lambda();

    public abstract void deltfcn();

    public abstract void clear();

    public abstract Component getModel();

    public double getTL() {
        return tL;
    }

    public void setTL(double tL) {
        this.tL = tL;
    }

    public double getTN() {
        return tN;
    }

    public void setTN(double tN) {
        this.tN = tN;
    }

    public SimulationClock getClock() {
        return clock;
    }
}
