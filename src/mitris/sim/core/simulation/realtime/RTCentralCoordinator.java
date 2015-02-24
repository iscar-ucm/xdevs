/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation.realtime;

import java.util.logging.Level;
import java.util.logging.Logger;

import mitris.logger.core.MitrisLogger;
import mitris.sim.core.Constants;
import mitris.sim.core.lib.examples.Efp;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.simulation.api.SimulationClock;
import mitris.sim.core.simulation.parallel.CoordinatorParallel;

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
        MitrisLogger.setup(Level.FINE);
        Efp efp = new Efp("EFP", 1, 3, 30);
        RTCentralCoordinator coordinator = new RTCentralCoordinator(efp);
        coordinator.initialize();
        coordinator.simulate(60.0);
    }
}
