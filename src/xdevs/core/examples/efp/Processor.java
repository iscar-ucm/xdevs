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
package xdevs.core.examples.efp;

import org.w3c.dom.Element;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Processor extends Atomic {

    protected Port<Job> iIn = new Port<>("iIn");
    protected Port<Job> oOut = new Port<>("oOut");
    protected Job currentJob = null;
    protected double processingTime;
    protected double clock; // *

    public Processor(String name, double processingTime) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
        this.processingTime = processingTime;
        this.clock = 0; // *

    }

    public Processor(Element xmlAtomic) {
        this(xmlAtomic.getAttribute("name"), 
             Double.parseDouble(((Element)(xmlAtomic.getElementsByTagName("constructor-arg").item(0))).getAttribute("value")));
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
        clock += super.getSigma();
        super.passivate();
    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        clock += e;
        if (super.phaseIs("passive")) {
            currentJob = iIn.getSingleValue();
            super.holdIn("active", processingTime);
            currentJob.time = clock;
        }
    }

    @Override
    public void lambda() {
        oOut.addValue(currentJob);
    }
}
