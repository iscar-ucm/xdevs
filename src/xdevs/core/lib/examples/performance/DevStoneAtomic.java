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

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.util.Dhrystone;

/**
 * Atomic model to study the performance using the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 *
 */
public class DevStoneAtomic extends Atomic {
    
    private static final Logger logger = Logger.getLogger(DevStoneAtomic.class.getName());
    
    public InPort<Integer> iIn = new InPort<>("in");
    public OutPort<Integer> oOut = new OutPort<>("out");
    protected LinkedList<Integer> outValues = new LinkedList<>();
    protected Dhrystone dhrystone;
    
    protected double preparationTime;
    protected double intDelayTime;
    protected double extDelayTime;
    
    public static long NUM_DELT_INTS = 0;
    public static long NUM_DELT_EXTS = 0;
    public static long NUM_OF_EVENTS = 0;
    
    public DevStoneAtomic(String name, DevStoneProperties properties) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
        this.preparationTime = properties.getPropertyAsDouble(DevStoneProperties.PREPARATION_TIME);
        this.intDelayTime = properties.getPropertyAsDouble(DevStoneProperties.INT_DELAY_TIME);
        this.extDelayTime = properties.getPropertyAsDouble(DevStoneProperties.INT_DELAY_TIME);
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
        NUM_DELT_INTS++;
        outValues.clear();
        Dhrystone.execute(intDelayTime);
        super.passivate();
    }
    
    @Override
    public void deltext(double e) {
        NUM_DELT_EXTS++;
        Dhrystone.execute(extDelayTime);
        if (!iIn.isEmpty()) {
            Collection<Integer> values = iIn.getValues();
            NUM_OF_EVENTS += values.size();
            for (Integer value : values) {
                outValues.add(value);
            }
        }
        super.holdIn("active", preparationTime);
    }
    
    @Override
    public void lambda() {
        oOut.addValues(outValues);
    }
}
