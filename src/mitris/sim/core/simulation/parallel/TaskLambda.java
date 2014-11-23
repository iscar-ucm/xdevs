/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mitris.sim.core.simulation.parallel;

import java.util.concurrent.Callable;

import mitris.sim.core.simulation.AbstractSimulator;

/**
 *
 * @author jlrisco
 */
public class TaskLambda implements Callable<Double> {
    
    protected AbstractSimulator simulator;
    
    public TaskLambda(AbstractSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public Double call() {
        simulator.lambda();
        return simulator.getClock().getTime();
    }
}
