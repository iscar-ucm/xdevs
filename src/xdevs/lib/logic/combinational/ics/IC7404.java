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
package xdevs.lib.logic.combinational.ics;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author jlrisco
 */
public class IC7404 extends Atomic {

    public InPort<Integer> iPin1 = new InPort<>("pin1");
    public InPort<Integer> iPin3 = new InPort<>("pin3");
    public InPort<Integer> iPin5 = new InPort<>("pin5");
    public InPort<Integer> iPin7 = new InPort<>("pin7");
    public InPort<Integer> iPin9 = new InPort<>("pin9");
    public InPort<Integer> iPin11 = new InPort<>("pin11");
    public InPort<Integer> iPin13 = new InPort<>("pin13");
    public InPort<Integer> iPin14 = new InPort<>("pin14");

    public OutPort<Integer> oPin2 = new OutPort<>("pin2");
    public OutPort<Integer> oPin4 = new OutPort<>("pin4");
    public OutPort<Integer> oPin6 = new OutPort<>("pin6");
    public OutPort<Integer> oPin8 = new OutPort<>("pin8");
    public OutPort<Integer> oPin10 = new OutPort<>("pin10");
    public OutPort<Integer> oPin12 = new OutPort<>("pin12");

    protected Integer valueAtPin1 = null;
    protected Integer valueAtPin3 = null;
    protected Integer valueAtPin5 = null;
    protected Integer valueAtPin7 = null;
    protected Integer valueAtPin9 = null;
    protected Integer valueAtPin11 = null;
    protected Integer valueAtPin13 = null;
    protected Integer valueAtPin14 = null;

    protected Integer valueToPin2 = null;
    protected Integer valueToPin4 = null;
    protected Integer valueToPin6 = null;
    protected Integer valueToPin8 = null;
    protected Integer valueToPin10 = null;
    protected Integer valueToPin12 = null;

    protected double delay;

    public IC7404(String name, double delay) {
        super(name);
        super.addInPort(iPin1);
        super.addOutPort(oPin2);
        super.addInPort(iPin3);
        super.addOutPort(oPin4);
        super.addInPort(iPin5);
        super.addOutPort(oPin6);
        super.addInPort(iPin7);
        super.addOutPort(oPin8);
        super.addInPort(iPin9);
        super.addOutPort(oPin10);
        super.addInPort(iPin11);
        super.addOutPort(oPin12);
        super.addInPort(iPin13);
        super.addInPort(iPin14);
        this.delay = delay;
    }

    public IC7404(double delay) {
        this(IC7404.class.getName(), delay);
    }

    public IC7404(String name) {
        this(name, 100 * 1e-12);
    }

    public IC7404() {
        this(IC7404.class.getName());
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
        valueToPin2 = null;
        valueToPin4 = null;
        valueToPin6 = null;
        valueToPin8 = null;
        valueToPin10 = null;
        valueToPin12 = null;
        super.passivate();
    }
    
    @Override
    public void deltext(double e) {
        Integer tempValueAtPin1 = (iPin1.isEmpty())? null : iPin1.getSingleValue();
        Integer tempValueAtPin3 = (iPin3.isEmpty())? null : iPin3.getSingleValue();
        Integer tempValueAtPin5 = (iPin5.isEmpty())? null : iPin5.getSingleValue();
        Integer tempValueAtPin7 = (iPin7.isEmpty())? null : iPin7.getSingleValue();
        Integer tempValueAtPin9 = (iPin9.isEmpty())? null : iPin9.getSingleValue();
        Integer tempValueAtPin11 = (iPin11.isEmpty())? null : iPin11.getSingleValue();
        Integer tempValueAtPin13 = (iPin13.isEmpty())? null : iPin13.getSingleValue();
        Integer tempValueAtPin14 = (iPin14.isEmpty())? null : iPin14.getSingleValue();
        if (tempValueAtPin7 != null && !tempValueAtPin7.equals(valueAtPin7)) {
            valueAtPin7 = tempValueAtPin7;
        }
        if (tempValueAtPin14 != null && !tempValueAtPin14.equals(valueAtPin14)) {
            valueAtPin14 = tempValueAtPin14;
        }

        if (tempValueAtPin1 != null && !tempValueAtPin1.equals(valueAtPin1)) {
            valueAtPin1 = tempValueAtPin1;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin2 = 1 - valueAtPin1;
                super.holdIn("active", delay);
            }
        }
        
        if (tempValueAtPin3 != null && !tempValueAtPin3.equals(valueAtPin3)) {
            valueAtPin3 = tempValueAtPin3;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin4 = 1 - valueAtPin3;
                super.holdIn("active", delay);
            }
        }

        if (tempValueAtPin5 != null && !tempValueAtPin5.equals(valueAtPin5)) {
            valueAtPin5 = tempValueAtPin5;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin6 = 1 - valueAtPin5;
                super.holdIn("active", delay);
            }
        }

        if (tempValueAtPin9 != null && !tempValueAtPin9.equals(valueAtPin9)) {
            valueAtPin9 = tempValueAtPin9;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin8 = 1 - valueAtPin9;
                super.holdIn("active", delay);
            }
        }

        if (tempValueAtPin11 != null && !tempValueAtPin11.equals(valueAtPin11)) {
            valueAtPin11 = tempValueAtPin11;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin10 = 1 - valueAtPin11;
                super.holdIn("active", delay);
            }
        }

        if (tempValueAtPin13 != null && !tempValueAtPin13.equals(valueAtPin13)) {
            valueAtPin13 = tempValueAtPin13;
            if(valueAtPin14.equals(1) && valueAtPin7.equals(0)) {
                valueToPin12 = 1 - valueAtPin13;
                super.holdIn("active", delay);
            }
        }
    }
    
    @Override
    public void lambda() {
        if(valueToPin2!=null) {
            oPin2.addValue(valueToPin2);
        }
        if(valueToPin4!=null) {
            oPin4.addValue(valueToPin4);
        }
        if(valueToPin6!=null) {
            oPin6.addValue(valueToPin6);
        }
        if(valueToPin8!=null) {
            oPin8.addValue(valueToPin8);
        }
        if(valueToPin10!=null) {
            oPin10.addValue(valueToPin10);
        }
        if(valueToPin12!=null) {
            oPin12.addValue(valueToPin12);
        }
    }
}
