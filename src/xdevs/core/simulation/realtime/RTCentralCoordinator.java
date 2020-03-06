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
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.examples.efp.Efp;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author jlrisco
 */
public class RTCentralCoordinator extends CoordinatorParallel implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(RTCentralCoordinator.class.getName());
    protected double timeInterval;
    protected Thread myThread;
    protected int timeScale = 1000;

    public RTCentralCoordinator(Coupled model) {
        super(new SimulationClock(System.currentTimeMillis() / 1000.0), model);
    }

    @Override
    public void simulate(double timeInterval) {
        this.timeInterval = timeInterval;
        myThread = new Thread(this);
        myThread.start();
    }

    public void setTimeScale(double realTimeFactor) {
        // convert the given time factor to milliseconds
        timeScale = (int) Math.floor(1000 * realTimeFactor);
        System.out.println("Time Scale factor: " + realTimeFactor);
    }

    @Override
    public void run() {
        long delay;
        clock.setTime(tN);
        double tF = clock.getTime() + timeInterval;
        while (clock.getTime() < Constants.INFINITY && clock.getTime() < tF) {
            //delay = (long) (1000 * clock.getTime() - System.currentTimeMillis())*timeScale;           
            delay = (long) (clock.getTime() - tL) * timeScale;
            delay = Math.max(delay, 0);
            if (delay > 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    LOGGER.severe(ex.getLocalizedMessage());
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
        double timeStart = System.currentTimeMillis();
        DevsLogger.setup(Level.FINE);
        Efp efp = new Efp("EFP", 1, 3, 20);
        RTCentralCoordinator coordinator = new RTCentralCoordinator(efp);
        coordinator.initialize();
        coordinator.setTimeScale(0.1);
        coordinator.simulate(60.0);
        double timeEnd = System.currentTimeMillis();
        System.out.println("Total execution time: " + (timeEnd - timeStart) + "ms");
    }
}
