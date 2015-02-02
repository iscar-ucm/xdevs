package mitris.sim.core.simulation;

import mitris.sim.core.simulation.api.SimulatorInterface;
import mitris.sim.core.simulation.api.SimulationClock;

/**
 *
 * @author jlrisco
 */
public abstract class AbstractSimulator implements SimulatorInterface {

	protected SimulationClock clock;
	protected double tL; // Time of last event
	protected double tN; // Time of next event

	public AbstractSimulator(SimulationClock clock) {
		this.clock = clock;
	}

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
