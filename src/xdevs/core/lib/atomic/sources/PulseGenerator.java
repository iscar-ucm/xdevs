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
 * @author jlrisco
 */
public class PulseGenerator extends Atomic {

    public OutPort<Double> portOut = new OutPort<>("portOut");
    protected double amplitude;
    protected double pulseWidth;
    protected double period;
    protected double phaseDelay;

    public PulseGenerator(String name, double amplitude, double pulseWidth, double period, double phaseDelay) {
    	super(name);
        super.addOutPort(portOut);
        this.amplitude = amplitude;
        this.pulseWidth = pulseWidth;
        this.period = period;
        this.phaseDelay = phaseDelay;
    }
    
    @Override
    public void initialize() {
        super.holdIn("delay", 0);    	
    }

    @Override
    public void exit() {
    }

    @Override
    public void deltint() {
        if (super.phaseIs("delay")) {
            super.holdIn("high", phaseDelay);
        } else if (super.phaseIs("high")) {
            super.holdIn("low", pulseWidth);
        } else if (super.phaseIs("low")) {
            super.holdIn("high", period - pulseWidth);
        }
    }

    @Override
    public void deltext(double e) {
    }

    @Override
    public void lambda() {
        if (super.phaseIs("delay")) {
            portOut.addValue(0.0);
        } else if (super.phaseIs("high")) {
            portOut.addValue(amplitude);
        } else if (super.phaseIs("low")) {
            portOut.addValue(0.0);
        }
    }

    public static void main(String[] args) {
        Coupled pulseExample = new Coupled("pulseExample");
        PulseGenerator pulse = new PulseGenerator("pulse",10, 3, 5, 5);
        pulseExample.addComponent(pulse);
        Console console = new Console("console");
        pulseExample.addComponent(console);
        pulseExample.addCoupling(pulse, pulse.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(pulseExample);
        coordinator.initialize();
        coordinator.simulate(30.0);
        coordinator.exit();
    }
}
