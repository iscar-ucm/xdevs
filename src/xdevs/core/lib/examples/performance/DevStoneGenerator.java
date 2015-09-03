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
package xdevs.core.lib.examples.performance;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.OutPort;

/**
 * Events generator for the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 */
public class DevStoneGenerator extends Atomic {

    public OutPort<Integer> oOut = new OutPort<>("out");
    protected double preparationTime;
    protected double period;
    protected int counter = 1;
    protected int maxEvents = Integer.MAX_VALUE;

    public DevStoneGenerator(String name, DevStoneProperties properties, int maxEvents) {
        super(name);
        super.addOutPort(oOut);
        this.preparationTime = properties.getPropertyAsDouble(DevStoneProperties.PREPARATION_TIME);
        this.period = properties.getPropertyAsDouble(DevStoneProperties.GENERATOR_PERIOD);
        this.maxEvents = maxEvents;
    }

    @Override
    public void initialize() {
        counter = 1;
        this.holdIn("active", preparationTime);
    }

    @Override
    public void deltint() {
        counter++;
        if (counter > maxEvents) {
            super.passivate();
        } else {
            this.holdIn("active", period);
        }
    }

    @Override
    public void deltext(double e) {
        super.passivate();
    }

    @Override
    public void lambda() {
        oOut.addValue(counter);
    }
}
