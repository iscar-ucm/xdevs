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
package xdevs.lib.external;

/**
 *
 * @author José L. Risco-Martín
 */
public class ProcessorDevsJava extends genDevs.modeling.atomic {

    protected JobDevsJava currentJob = null;
    protected double processingTime;

    public ProcessorDevsJava(String name, double processingTime) {
        super(name);
        super.addInport("iIn");
        super.addOutport("oOut");
        this.processingTime = processingTime;
    }

    @Override
    public void initialize() {
        super.passivate();
    }

    @Override
    public void deltint() {
        super.passivate();
        currentJob = null;
    }

    @Override
    public void deltext(double e, genDevs.modeling.message x) {
        Continue(e);
        if (super.phaseIs("passive")) {
            for (int i = 0; i < x.getLength(); i++) {
                if (messageOnPort(x, "iIn", i)) {
                    currentJob = (JobDevsJava) x.getValOnPort("iIn", i);
                    this.holdIn("active", processingTime);
                }
            }
        }
    }

    @Override
    public genDevs.modeling.message out() {
        genDevs.modeling.message m = new genDevs.modeling.message();
        m.add(makeContent("oOut", currentJob));
        return m;
    }

}
