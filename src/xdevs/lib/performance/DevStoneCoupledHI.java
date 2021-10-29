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

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * Coupled model to study the performance HI DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHI extends DevStone {

    public DevStoneCoupledHI(String prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime) {
        super(prefix + (depth - 1), width, depth);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHI coupled = new DevStoneCoupledHI(prefix, width, depth - 1, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
                super.addComponent(atomic);
                super.addCoupling(iIn, atomic.iIn);
                if (atomicPrev != null) {
                    super.addCoupling(atomicPrev.oOut, atomic.iIn);
                }
                atomicPrev = atomic;
            }
        }
    }

    public DevStoneCoupledHI(String prefix, int width, int depth, double preparationTime, RealDistribution distribution) {
        super(prefix + (depth - 1), width, depth);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, distribution);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHI coupled = new DevStoneCoupledHI(prefix, width, depth - 1, preparationTime, distribution);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, preparationTime, distribution);
                super.addComponent(atomic);
                super.addCoupling(iIn, atomic.iIn);
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

    @Override
    public int getNumOfAtomic(int width, int depth) {
        return (width - 1) * (depth - 1) + 1;
    }
}
