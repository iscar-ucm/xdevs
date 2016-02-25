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
 *  - José Luis Risco Martín <jlrisco@ucm.es>
 *  - Saurabh Mittal <smittal@duniptech.com>
 */
package xdevs.core.simulation.realtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import xdevs.core.util.Constants;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.api.SimulationClock;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.test.efp.Efp;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author jlrisco
 */
public class RTCentralCoordinator extends CoordinatorParallel implements Runnable {

    private static final Logger logger = Logger.getLogger(RTCentralCoordinator.class.getName());
    protected double timeInterval;
    protected Thread myThread;

    public RTCentralCoordinator(Coupled model) {
        super(new SimulationClock(System.currentTimeMillis() / 1000.0), model);
    }

    @Override
    public void simulate(double timeInterval) {
        this.timeInterval = timeInterval;
        myThread = new Thread(this);
        myThread.start();
    }

    @Override
    public void run() {
        long delay;
        clock.setTime(tN);
        double tF = clock.getTime() + timeInterval;
        while (clock.getTime() < Constants.INFINITY && clock.getTime() < tF) {
            delay = (long) (1000 * clock.getTime() - System.currentTimeMillis());
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    logger.severe(ex.getLocalizedMessage());
                }
            }
            lambda();
            deltfcn();
            clear();
            clock.setTime(tN);
        }
        executor.shutdown();
    }

    public static void main(String[] args) {
        DevsLogger.setup(Level.FINE);
        Efp efp = new Efp("EFP", 1, 3, 30);
        RTCentralCoordinator coordinator = new RTCentralCoordinator(efp);
        coordinator.initialize();
        coordinator.simulate(60.0);
    }
}
