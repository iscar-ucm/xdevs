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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.RealDistribution;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHO extends DevStone {
    
    private static final Logger LOGGER = Logger.getLogger(DevStoneCoupledHO.class.getName());

    public Port<Integer> iInAux = new Port<>("inAux");
    public Port<Integer> oOutAux = new Port<>("outAux");

    public DevStoneCoupledHO(String name) {
        super(name);
        super.addInPort(iInAux);
        super.addOutPort(oOutAux);
    }

    public DevStoneCoupledHO(String prefix, int width, int depth, double preparationTime, double intDelayTime, double extDelayTime) {
        this(prefix + (depth - 1));
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHO coupled = new DevStoneCoupledHO(prefix, width, depth - 1, preparationTime, intDelayTime, extDelayTime);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(iIn, coupled.iInAux);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, preparationTime, intDelayTime, extDelayTime);
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

    public DevStoneCoupledHO(String prefix, int width, int depth, double preparationTime, RealDistribution distribution) {
        this(prefix + (depth - 1));
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, preparationTime, distribution);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHO coupled = new DevStoneCoupledHO(prefix, width, depth - 1, preparationTime, distribution);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(iIn, coupled.iInAux);
            super.addCoupling(coupled.oOut, oOut);
            DevStoneAtomic atomicPrev = null;
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("A" + (i + 1) + "_" + name, preparationTime, distribution);
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
    
    public static void main(String[] args) {
        // Benchmark type and parameters
        int width = 6;
        int depth = 4;
        double intDelayTime = 1.618;
        double extDelayTime = 1.618;

        // Generator parameters:
        double preparationTime = 0.0;
        double period = 1.0;
        int maxEvents = 1;

        // Atomic number of internal and external transitions, as well as number of events
        DevStoneAtomic.NUM_DELT_INTS = 0;
        DevStoneAtomic.NUM_DELT_EXTS = 0;
        DevStoneAtomic.NUM_OF_EVENTS = 0;

        Coupled framework = new Coupled(DevStoneCoupledHO.class.getSimpleName());
        // Generator
        DevStoneGenerator generator = new DevStoneGenerator("Generator", preparationTime, period, maxEvents);
        framework.addComponent(generator);
        // HO model
        DevStoneCoupledHO stoneCoupled = new DevStoneCoupledHO("C", width, depth, preparationTime, intDelayTime, extDelayTime);        
        LOGGER.info(stoneCoupled.toXml());
        // In the following, stoneCoupled is flattened. The idea is to have a fair comparison between sequential, parallel and distributed models.
        // Since parallel and distributed models are always flattened, we flatten also sequential executions
        stoneCoupled = (DevStoneCoupledHO)stoneCoupled.flatten();
        LOGGER.info(stoneCoupled.toXml());
        framework.addComponent(stoneCoupled);
        // Couplings
        framework.addCoupling(generator.oOut, stoneCoupled.iIn);
        framework.addCoupling(generator.oOut, stoneCoupled.iInAux);

        Coordinator coordinator = new Coordinator(framework, false);
        //CoordinatorParallel coordinator = new CoordinatorParallel(framework);
        coordinator.initialize();
        long start = System.currentTimeMillis();
        DevsLogger.setup(Level.INFO);
        coordinator.simulate(Long.MAX_VALUE);
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        LOGGER.info("MAXEVENTS;WIDTH;DEPTH;NUM_DELT_INTS;NUM_DELT_EXTS;NUM_OF_EVENTS;TIME");
        String stats = maxEvents + ";" + width + ";" + depth + ";" + DevStoneAtomic.NUM_DELT_INTS + ";" + DevStoneAtomic.NUM_DELT_EXTS + ";" + DevStoneAtomic.NUM_OF_EVENTS + ";" + time;
        LOGGER.info(stats);        
    }

    @Override
    public int getNumOfAtomic(int width, int depth) {
        return (width - 1) * (depth - 1) + 1;
    }

}
