/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation;

/**
 *
 * @author jlrisco
 */
public class SimulationClock {

    protected double time;

    public SimulationClock(double time) {
        this.time = time;
    }

    public SimulationClock() {
        this(0);
    }

    public double getTime() {
        return time;
    }
    
    public void setTime(double time) {
        this.time = time;
    }
}
