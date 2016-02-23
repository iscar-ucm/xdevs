/*
 * Copyright (C) 2014-2016 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.lib.logic.combinational;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author jlrisco
 */
public class Nand2 extends Atomic {

    public InPort<Integer> iIn0 = new InPort<>("In0");
    public InPort<Integer> iIn1 = new InPort<>("In1");
    public OutPort<Integer> oOut = new OutPort<>("Out");

    protected double delay;
    protected Integer valueToOut = null;
    protected Integer valueAtIn0 = null;
    protected Integer valueAtIn1 = null;

    public Nand2(String name, double delay) {
        super(name);
        super.addInPort(iIn0);
        super.addInPort(iIn1);
        super.addOutPort(oOut);
        this.delay = delay;
    }

    public Nand2(String name) {
        this(name, 0);
    }

    public Nand2() {
        super(Nand2.class.getName());
    }

    @Override
    public void initialize() {
        super.passivate();
    }

    @Override
    public void exit() {
    }

    @Override
    public void deltint() {
        super.passivate();
    }

    @Override
    public void deltext(double e) {
        Integer tempValueAtIn0 = iIn0.getSingleValue();
        if (tempValueAtIn0 != null && !tempValueAtIn0.equals(valueAtIn0)) {
            valueAtIn0 = tempValueAtIn0;
            super.holdIn("active", delay);
        }
        Integer tempValueAtIn1 = iIn1.getSingleValue();
        if (tempValueAtIn1 != null && !tempValueAtIn1.equals(valueAtIn1)) {
            valueAtIn1 = tempValueAtIn1;
            super.holdIn("active", delay);
        }
        if (super.phaseIs("active") && valueAtIn0 != null && valueAtIn1 != null) {
            valueToOut = valueAtIn0 & valueAtIn1;
        }
    }

    @Override
    public void lambda() {
        oOut.addValue(valueToOut);
    }
}
