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

import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHO extends DevStoneCoupled {

    public InPort<Integer> iInAux = new InPort<>("inAux");
    public OutPort<Integer> oOutAux = new OutPort<>("outAux");

    public DevStoneCoupledHO(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        super.addInPort(iInAux);
        super.addOutPort(oOutAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHO coupled = new DevStoneCoupledHO(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(iIn, coupled.iInAux);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                super.addCoupling(atomic.oOut, oOutAux);
                if (atomicPrev != null) {
                    super.addCoupling(atomicPrev.oOut, atomic.iIn);
                }
                atomicPrev = atomic;
            }
        }
    }

    @Override
    public int getNumDeltExts(int maxEvents, int width, int depth) {
        return maxEvents * (((width * width - width) / 2) * (depth - 1) + 1);

    }

    @Override
    public int getNumDeltInts(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }

    @Override
    public long getNumOfEvents(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }
}
