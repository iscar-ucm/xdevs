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
 *  - José Luis Risco Martín
 */
package xdevs.lib.logic.sequential;

import xdevs.core.atomic.sinks.Console;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.simulation.Coordinator;

/**
 * Generate clock signal for logic systems.
 * 
 * The Clock block generates a clock signal for logic systems. The Clock block 
 * outputs 1 for the first half of the specified sample period and 0 for the 
 * other half of the sample period. You can use the Clock block to control 
 * the execution of the D Flip-Flop, and other enabled and triggered subsystems.
 * 
 * @author José Luis Risco Martín
 */
public class Clock extends Atomic {

    public InPort<Object> iStop = new InPort<>("iStop");
    public OutPort<Integer> oClk = new OutPort<>("oClk");
    protected double period;
    protected double semiPeriod;
    protected int nextValue;
    protected long count;

    /**
     * 
     * @param name Name of the component
     * @param period Specify the sample period. By default, the Clock block outputs 
     * 1 for the first half of the sample period.
     * @param initialValue Determines the first output of the clock. If initialValue == 0,
     * the first output is 0. If initialValue != 0, the first output is 1.
     */
    public Clock(String name, double period, int initialValue) {
    	super(name);
        super.addInPort(iStop);
        super.addOutPort(oClk);
        this.period = period;
        this.semiPeriod = period/2.0;
        this.nextValue = 0;
        this.count = 0L;
        if (initialValue != 0) {
            this.nextValue = 1;
        }
    }

    public Clock(String name, double period) {
        this(name, period, 1);
    }

    public Clock(String name) {
        this(name, 1);
    }
    
    public Clock() {
        this(Clock.class.getName());
    }

    @Override
    public void initialize() {
        super.activate();    	
    }

    @Override
    public void exit() {
    }

    @Override
    public void deltint() {
        // New semiperiod:
        count++;
        // New Value:
        nextValue = 1 - nextValue;
        // Nex value in a semiperiod:
        super.holdIn("active", semiPeriod);
    }

    @Override
    public void deltext(double e) {
        if (!iStop.isEmpty()) {
            super.passivate();
        }
    }

    @Override
    public void lambda() {
        oClk.addValue(nextValue);
    }

    public long getCount() {
        return count;
    }

    public static void main(String[] args) {
        Coupled clockExample = new Coupled("clockExample");
        Clock clock = new Clock("clock",10.0);
        clockExample.addComponent(clock);
        Console console = new Console("console");
        clockExample.addComponent(console);
        clockExample.addCoupling(clock, clock.oClk, console, console.iIn);
        Coordinator coordinator = new Coordinator(clockExample);
        coordinator.initialize();
        coordinator.simulate(30.0);
        coordinator.exit();
    }
}
