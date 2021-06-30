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

import java.util.ArrayList;

import org.apache.commons.math3.distribution.RealDistribution;
import xdevs.core.modeling.Port;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHOmem extends DevStone {

    public Port<Integer> iInAux = new Port<>("inAux");

    public DevStoneCoupledHOmem(String prefix, int width, int depth, double preparationTime, double intDelayTime,
            double extDelayTime) {
        super(prefix + (depth - 1), width, depth);
        super.addInPort(iInAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHOmem coupled = new DevStoneCoupledHOmem(prefix, width, depth - 1, preparationTime,
                    intDelayTime, extDelayTime);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            // First layer of atomic models:
            ArrayList<DevStoneAtomic> prevLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL1_" + (i + 1) + "_" + name, preparationTime, intDelayTime,
                        extDelayTime);
                super.addComponent(atomic);
                super.addCoupling(atomic.oOut, coupled.iInAux);
                prevLayer.add(atomic);
            }
            // Second layer of atomic models:
            ArrayList<DevStoneAtomic> currentLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL2_" + (i + 1) + "_" + name, preparationTime, intDelayTime,
                        extDelayTime);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                currentLayer.add(atomic);
            }
            for (int i = 0; i < currentLayer.size(); ++i) {
                DevStoneAtomic fromAtomic = currentLayer.get(i);
                for (int j = 0; j < prevLayer.size(); ++j) {
                    DevStoneAtomic toAtomic = prevLayer.get(j);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
            }
        }
    }

    public DevStoneCoupledHOmem(String prefix, int width, int depth, double preparationTime, RealDistribution distribution) {
        super(prefix + (depth - 1), width, depth);
        super.addInPort(iInAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, distribution);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHOmem coupled = new DevStoneCoupledHOmem(prefix, width, depth - 1, preparationTime,
                    distribution);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            // First layer of atomic models:
            ArrayList<DevStoneAtomic> prevLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL1_" + (i + 1) + "_" + name, preparationTime, distribution);
                super.addComponent(atomic);
                super.addCoupling(atomic.oOut, coupled.iInAux);
                prevLayer.add(atomic);
            }
            // Second layer of atomic models:
            ArrayList<DevStoneAtomic> currentLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL2_" + (i + 1) + "_" + name, preparationTime, distribution);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                currentLayer.add(atomic);
            }
            for (int i = 0; i < currentLayer.size(); ++i) {
                DevStoneAtomic fromAtomic = currentLayer.get(i);
                for (int j = 0; j < prevLayer.size(); ++j) {
                    DevStoneAtomic toAtomic = prevLayer.get(j);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
            }
        }
    }

    @Override
    public int getNumDeltExts(int maxEvents, int width, int depth) {
        return maxEvents * (1 + 2 * (depth - 1) * (width - 1));
    }

    @Override
    public int getNumDeltInts(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }

    @Override
    public long getNumOfEvents(int maxEvents, int width, int depth) {
        long numEvents = 1;
        int gamma1 = width - 1;
        for (int d = 1; d < depth; ++d) {
            numEvents += (Math.pow(gamma1, 2 * d) + Math.pow(gamma1, 2 * d - 1));
        }
        return maxEvents * numEvents;
    }

    @Override
    public int getNumOfAtomic(int width, int depth) {
        return 2 * (width - 1) * (depth - 1) + 1;
    }

}
