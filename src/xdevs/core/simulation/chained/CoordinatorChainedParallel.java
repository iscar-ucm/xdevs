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
 *  - Román Cárdenas Rodríguez <r.cardenas@upm.es>
 */
package xdevs.core.simulation.chained;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.test.efp.Efp;
import xdevs.core.util.DevsLogger;

import java.util.logging.Level;
import java.util.logging.Logger;


public class CoordinatorChainedParallel extends CoordinatorParallel {

    private static final Logger LOGGER = Logger.getLogger(CoordinatorChainedParallel.class.getName());

    public CoordinatorChainedParallel(SimulationClock clock, Coupled model, int numberOfThreads) {
        super(clock, model, numberOfThreads);
        this.model.in2Out();
        this.model.toChain();
    }

    public CoordinatorChainedParallel(SimulationClock clock, Coupled model) {
        this(clock, model, Runtime.getRuntime().availableProcessors());
    }

    public CoordinatorChainedParallel(Coupled model) {
        this(new SimulationClock(), model, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void lambda() {
        try {
            executor.invokeAll(lambdaTasks);
        } catch (InterruptedException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
    }

    @Override
    public void deltfcn() {
        try {
            executor.invokeAll(deltfcnTasks);
        } catch (InterruptedException ee) {
            LOGGER.severe(ee.getLocalizedMessage());
        }
        tL = clock.getTime();
        tN = tL + ta();
    }

    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        Efp efp = new Efp("EFP", 1, 3, 1000000);
        CoordinatorChainedParallel coordinator = new CoordinatorChainedParallel(efp);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }
}
