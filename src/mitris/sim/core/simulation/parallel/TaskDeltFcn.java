/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation.parallel;

import java.util.concurrent.Callable;

import mitris.sim.core.simulation.api.SimulatorInterface;

/**
 *
 * @author jlrisco
 */
public class TaskDeltFcn implements Callable<Double> {

    protected SimulatorInterface simulator;

    public TaskDeltFcn(SimulatorInterface simulator) {
        this.simulator = simulator;
    }

    @Override
    public Double call() {
        simulator.deltfcn();
        return simulator.getClock().getTime();
    }
}
