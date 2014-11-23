package mitris.sim.core.simulation.api;

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
