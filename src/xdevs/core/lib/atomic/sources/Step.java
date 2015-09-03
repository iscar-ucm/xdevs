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
package xdevs.core.lib.atomic.sources;

import xdevs.core.atomic.sinks.Console;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.OutPort;
import xdevs.core.simulation.Coordinator;

/**
 *
 * @author José Luis Risco Martín
 */
public class Step extends Atomic {

    public OutPort<Double> portOut = new OutPort<Double>("portOut");
    protected double initialValue;
    protected double stepTime;
    protected double finalValue;

    public Step(String name, double initialValue, double stepTime, double finalValue) {
    	super(name);
        super.addOutPort(portOut);
        this.initialValue = initialValue;
        this.stepTime = stepTime;
        this.finalValue = finalValue;
    }
    
    public void initialize() {
        super.holdIn("initialValue", 0.0);    	
    }

    @Override
    public void deltint() {
        if (super.phaseIs("initialValue")) {
            super.holdIn("finalValue", stepTime);
        } else if (super.phaseIs("finalValue")) {
            super.passivate();
        }
    }

    @Override
    public void deltext(double e) {
    }

    @Override
    public void lambda() {
        if (super.phaseIs("initialValue")) {
            portOut.addValue(initialValue);
        } else if (super.phaseIs("finalValue")) {
            portOut.addValue(finalValue);
        }
    }

    public static void main(String[] args) {
        Coupled stepExample = new Coupled("stepExample");
        Step step = new Step("step", 0, 15, 10);
        stepExample.addComponent(step);
        Console console = new Console("console");
        stepExample.addComponent(console);
        stepExample.addCoupling(step, step.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(stepExample);
        coordinator.simulate(30.0);
    }
}
