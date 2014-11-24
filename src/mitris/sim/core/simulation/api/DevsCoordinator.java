package mitris.sim.core.simulation.api;

import java.util.Collection;

/**
 *
 * @author José Luis Risco Martín
 */
public interface DevsCoordinator extends DevsSimulator {
	public Collection<DevsSimulator> getSimulators();
	public void propagateOutput();
	public void propagateInput();
	public void simulate(long numIterations);
	public void simulate(double timeInterval);
}
