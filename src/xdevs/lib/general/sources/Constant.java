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
package xdevs.lib.general.sources;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author jlrisco
  */
public class Constant<E extends Number> extends Atomic {

    public OutPort<E> oOut = new OutPort<>("out");

    protected E valueToOut;
    protected double stepTime;

    public Constant(String name, E value, double stepTime) {
        super(name);
        super.addOutPort(oOut);
        this.valueToOut = value;
        this.stepTime = stepTime;
    }

    public Constant(String name, E value) {
        this(name, value, 0);
    }

    public Constant(E value) {
        this(Constant.class.getName(), value, 0);
    }

    @Override
    public void initialize() {
        super.holdIn("active", stepTime);
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
    }

    @Override
    public void lambda() {
        oOut.addValue(valueToOut);
    }
}
