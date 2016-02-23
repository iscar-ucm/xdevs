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
package xdevs.lib.logic.sequential;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author jlrisco
 */
public class FlipFlopD extends Atomic {

    public InPort<Integer> iClk = new InPort<>("Clk");
    public InPort<Integer> iD = new InPort<>("D");
    public OutPort<Integer> oQ = new OutPort<>("Q");

    protected double delay;
    protected Integer valueAtClk = null;
    protected Integer valueAtD = null;
    protected Integer valueToQ = 0;

    public FlipFlopD(String name, double delay) {
        super(name);
        super.addInPort(iClk);
        super.addInPort(iD);
        super.addOutPort(oQ);
        this.delay = delay;
    }

    public FlipFlopD(String name) {
        this(name, 0);
    }

    public FlipFlopD() {
        this(FlipFlopD.class.getName());
    }

    @Override
    public void initialize() {
        super.holdIn("active", 0);
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
        // Primero miramos si hay algo en D:
        Integer tempValueAtD = iD.getSingleValue();
        if (tempValueAtD != null) {
            valueAtD = tempValueAtD;
        }
        // Luego miramos si hay flanco de reloj
        Integer tempValueAtClk = iClk.getSingleValue();
        if (tempValueAtClk != null) {
            if (tempValueAtClk == 1 && (valueAtClk == null || valueAtClk == 0)) {
                valueToQ = valueAtD;
                // Es tiempo de enviar la salida.
                super.holdIn("active", delay);
            }
            valueAtClk = tempValueAtClk;
        }
    }

    @Override
    public void lambda() {
        oQ.addValue(valueToQ);
    }

}
