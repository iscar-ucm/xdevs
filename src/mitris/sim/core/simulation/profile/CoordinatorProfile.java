package mitris.sim.core.simulation.profile;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;

import mitris.logger.core.MitrisLogger;
import mitris.sim.core.lib.examples.Efp;
import mitris.sim.core.simulation.Coordinator;
import mitris.sim.core.simulation.api.DevsCoordinator;
import mitris.sim.core.simulation.api.DevsSimulator;

/**
 *
 * @author José Luis Risco Martín
 */
public class CoordinatorProfile extends SimulatorProfile implements DevsCoordinator {

	//private static final Logger logger = Logger.getLogger(CoordinatorProfile.class.getName());
	
	protected DevsCoordinator realCoordinator;
	
	protected long numCallsToGetSimulators = 0;
	protected long timeUsedByGetSimulators = 0;
	protected long numCallsToPropagateOutput = 0;
	protected long timeUsedByPropagateOutput = 0;
	protected long numCallsToPropagateInput = 0;
	protected long timeUsedByPropagateInput = 0;
	protected double executionTime = 0.0;

	public CoordinatorProfile(DevsCoordinator realCoordinator) {
		super(realCoordinator);
		this.realCoordinator = realCoordinator;

		Collection<DevsSimulator> realSimulators = realCoordinator.getSimulators();
		Collection<DevsSimulator> profSimulators = new LinkedList<DevsSimulator>();
		for (DevsSimulator realSimulator : realSimulators) {
			if (realSimulator instanceof DevsCoordinator) {
				CoordinatorProfile profCoordinator = new CoordinatorProfile((DevsCoordinator)realSimulator);
				profSimulators.add(profCoordinator);
			} else if (realSimulator instanceof DevsSimulator) {
				SimulatorProfile profSimulator = new SimulatorProfile(realSimulator);
				profSimulators.add(profSimulator);
			}
		}
		realSimulators.clear();
		realSimulators.addAll(profSimulators);
	}

	@Override
	public Collection<DevsSimulator> getSimulators() {
		this.numCallsToGetSimulators++;
		long start = System.currentTimeMillis();
		Collection<DevsSimulator> result = realCoordinator.getSimulators();
		long end = System.currentTimeMillis();
		this.timeUsedByGetSimulators += (end - start);
		return result;
	}

	@Override
	public void propagateOutput() {
		this.numCallsToPropagateOutput++;
		long start = System.currentTimeMillis();
		realCoordinator.propagateOutput();
		long end = System.currentTimeMillis();
		this.timeUsedByPropagateOutput += (end - start);
	}

	@Override
	public void propagateInput() {
		this.numCallsToPropagateInput++;
		long start = System.currentTimeMillis();
		realCoordinator.propagateInput();
		long end = System.currentTimeMillis();
		this.timeUsedByPropagateInput += (end - start);
	}

	@Override
	public void simulate(long numIterations) {
		long start = System.currentTimeMillis();
		realCoordinator.simulate(numIterations);
		long end = System.currentTimeMillis();
		this.executionTime += (end - start);
	}

	@Override
	public void simulate(double timeInterval) {
		long start = System.currentTimeMillis();
		realCoordinator.simulate(timeInterval);
		long end = System.currentTimeMillis();
		this.executionTime += (end - start);
	} 
	
	public String getStats() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("========================================================================\n");
		buffer.append("Statistics for ").append(realCoordinator.getModel().getName()).append(":\n");
		buffer.append("========================================================================\n");
		buffer.append(super.getStats());
		buffer.append("numCallsToGetSimulators = ").append(numCallsToGetSimulators).append("\n");
		buffer.append("timeUsedByGetSimulators = ").append(timeUsedByGetSimulators).append(" ms\n");
		buffer.append("numCallsToPropagateOutput = ").append(numCallsToPropagateOutput).append("\n");
		buffer.append("timeUsedByPropagateOutput = ").append(timeUsedByPropagateOutput).append(" ms\n");
		buffer.append("numCallsToPropagateInput = ").append(numCallsToPropagateInput).append("\n");
		buffer.append("timeUsedByPropagateInput = ").append(timeUsedByPropagateInput).append(" ms\n");
		buffer.append("executionTime = ").append(executionTime).append(" ms\n");
		for(DevsSimulator simulator : realCoordinator.getSimulators()) {
			buffer.append(((SimulatorProfile)simulator).getStats());
		}
		return buffer.toString();
	}
	
    public static void main(String args[]) {
        MitrisLogger.setup(Level.INFO);
        Efp efp = new Efp("efp", 1, 3, 1000);
        CoordinatorProfile coordinator = new CoordinatorProfile(new Coordinator(efp, false));
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);        
        System.out.println(coordinator.getStats());
    }

}
