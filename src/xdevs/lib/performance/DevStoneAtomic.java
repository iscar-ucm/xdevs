/*
 * Copyright (C) 2014-2016 José Luis Risco Martín <jlrisco@ucm.es>
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
package xdevs.lib.performance;

import java.util.Collection;
import java.util.LinkedList;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import xdevs.core.util.RandomGenerator;
import xdevs.lib.util.Dhrystone;

/**
 * Atomic model to study the performance using the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 *
 */
public class DevStoneAtomic extends Atomic {
      
    public Port<Integer> iIn = new Port<>("in");
    public Port<Integer> oOut = new Port<>("out");
    protected LinkedList<Integer> outValues = new LinkedList<>();
    protected Dhrystone dhrystone;
    
    protected double preparationTime;
    protected double intDelayTime;
    protected double extDelayTime;
    
    public static long NUM_DELT_INTS = 0;
    public static long NUM_DELT_EXTS = 0;
    public static long NUM_OF_EVENTS = 0;
    
    public DevStoneAtomic(String name, double preparationTime, double intDelayTimeMax, double extDelayTimeMax, boolean randomGenerator) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
        this.preparationTime = preparationTime;
        this.intDelayTime = (randomGenerator == true) ? RandomGenerator.nextDouble(0, intDelayTimeMax) : intDelayTimeMax;
        this.extDelayTime = (randomGenerator == true) ? RandomGenerator.nextDouble(0, extDelayTimeMax) : extDelayTimeMax;
    }
    
    public DevStoneAtomic(String name, double preparationTime, double intDelayTime, double extDelayTime) {
        this(name, preparationTime, intDelayTime, extDelayTime, false);
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
