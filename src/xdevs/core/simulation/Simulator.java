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

import xdevs.core.modeling.Port;
import xdevs.core.modeling.Atomic;

/**
 *
 * @author José Luis Risco Martín
 */
public class Simulator extends AbstractSimulator {

    protected Atomic model;

    public Simulator(SimulationClock clock, Atomic model) {
        super(clock);
        this.model = model;
    }

    @Override
    public void initialize() {
        model.initialize();
        tL = clock.getTime();
        tN = tL + model.ta();
    }

    @Override
    public void exit() {
        model.exit();
    }

    @Override
    public double ta() {
        return model.ta();
    }

    @Override
    public void deltfcn() {
        double t = clock.getTime();
        boolean isInputEmpty = model.isInputEmpty();
        if (!isInputEmpty) {
            double e = t - tL;
            if (t == tN) {
                model.deltcon(e);
            } else {
                model.deltext(e);
            }
        } else if (t == tN)
            model.deltint();
        else
            return;
        tL = t;
        tN = tL + model.ta();
    }

    @Override
    public void lambda() {
        if (clock.getTime() == tN) {
            // System.out.println("lambda" + model.getName());
            model.lambda();
        }
    }

    @Override
    public void clear() {
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

    @Override
    public Atomic getModel() {
        return model;
    }

}
