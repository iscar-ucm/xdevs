/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation;

/**
 *
 * @author jlrisco
 */
public abstract class AbstractSimulator {

    protected SimulationClock clock;
    protected double tL; // Time of last event
    protected double tN; // Time of next event
    
    public AbstractSimulator(SimulationClock clock) {
        this.clock = clock;
    }

    abstract public double ta();

    abstract public void lambda();

    abstract public void deltfcn();

    abstract public void clear();
    
    public double getTL() {
        return tL;
    }

    public void setTL(double tL) {
        this.tL = tL;
    }

    public double getTN() {
        return tN;
    }

    public void setTN(double tN) {
        this.tN = tN;
    }

    public SimulationClock getClock() {
        return clock;
    }
}
