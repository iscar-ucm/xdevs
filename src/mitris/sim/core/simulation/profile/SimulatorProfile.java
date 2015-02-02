package mitris.sim.core.simulation.profile;

import mitris.sim.core.modeling.api.ComponentInterface;
import mitris.sim.core.simulation.api.SimulatorInterface;
import mitris.sim.core.simulation.api.SimulationClock;

/**
 *
 * @author José Luis Risco Martín
 */
public class SimulatorProfile implements SimulatorInterface {

	protected SimulatorInterface realSimulator;
	protected long numCallsToTa = 0;
	protected long timeUsedByTa = 0;
	protected long numCallsToDeltFcn = 0;
	protected long timeUsedByDeltFcn = 0;
	protected long numCallsToLambda = 0;
	protected long timeUsedByLambda = 0;
	protected long numCallsToClear = 0;
	protected long timeUsedByClear = 0;
	protected long numCallsToInitialize = 0;
	protected long timeUsedByInitialize = 0;
	protected long numCallsToGetTN = 0;
	protected long timeUsedByGetTN = 0;
	protected long numCallsToGetTL = 0;
	protected long timeUsedByGetTL = 0;
	protected long numCallsToSetTN = 0;
	protected long timeUsedBySetTN = 0;
	protected long numCallsToSetTL = 0;
	protected long timeUsedBySetTL = 0;
	protected long numCallsToGetClock = 0;
	protected long timeUsedByGetClock = 0;

	public SimulatorProfile(SimulatorInterface realSimulator) {
		this.realSimulator = realSimulator;
	}

	public double ta() {
		numCallsToTa++;
		long start = System.currentTimeMillis();
		double result = realSimulator.ta();
		long end = System.currentTimeMillis();
		timeUsedByTa += (end - start);
		return result;
	}

	public void deltfcn() {
		this.numCallsToDeltFcn++;
		long start = System.currentTimeMillis();
		realSimulator.deltfcn();
		long end = System.currentTimeMillis();
		this.timeUsedByDeltFcn += (end - start);
	}

	public void lambda() {
		this.numCallsToLambda++;
		long start = System.currentTimeMillis();
		realSimulator.lambda();
		long end = System.currentTimeMillis();
		this.timeUsedByLambda += (end - start);
	}

	public void clear() {
		this.numCallsToClear++;
		long start = System.currentTimeMillis();
		realSimulator.clear();
		long end = System.currentTimeMillis();
		this.timeUsedByClear += (end - start);
	}

	@Override
	public void initialize() {
		this.numCallsToInitialize++;
		long start = System.currentTimeMillis();
		realSimulator.initialize();
		long end = System.currentTimeMillis();
		this.timeUsedByInitialize += (end - start);

	}

	@Override
	public double getTL() {
		this.numCallsToGetTL++;
		long start = System.currentTimeMillis();
		double result = realSimulator.getTL();
		long end = System.currentTimeMillis();
		this.timeUsedByGetTL += (end - start);
		return result;
	}

	@Override
	public void setTL(double tL) {
		this.numCallsToSetTL++;
		long start = System.currentTimeMillis();
		realSimulator.setTL(tL);
		long end = System.currentTimeMillis();
		this.timeUsedBySetTL += (end - start);
	}

	@Override
	public double getTN() {
		this.numCallsToGetTN++;
		long start = System.currentTimeMillis();
		double result = realSimulator.getTN();
		long end = System.currentTimeMillis();
		this.timeUsedByGetTN += (end - start);
		return result;
	}

	@Override
	public void setTN(double tN) {
		this.numCallsToSetTN++;
		long start = System.currentTimeMillis();
		realSimulator.setTN(tN);
		long end = System.currentTimeMillis();
		this.timeUsedBySetTN += (end - start);
	}

	@Override
	public SimulationClock getClock() {
		this.numCallsToGetClock++;
		long start = System.currentTimeMillis();
		SimulationClock result = realSimulator.getClock();
		long end = System.currentTimeMillis();
		this.timeUsedByGetClock += (end - start);
		return result;
	}
	
	public ComponentInterface getModel() {
		return realSimulator.getModel();
	}

	public String getStats() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Statistics for ").append(realSimulator.getModel().getName()).append(":\n");
		buffer.append("----------------------------------------------------------------------\n");
		buffer.append("numCallsToTa = ").append(numCallsToTa).append("\n");
		buffer.append("timeUsedByTa = ").append(timeUsedByTa).append(" ms\n");
		buffer.append("numCallsToDeltFcn = ").append(numCallsToDeltFcn).append("\n");
		buffer.append("timeUsedByDeltFcn = ").append(timeUsedByDeltFcn).append(" ms\n");
		buffer.append("numCallsToLambda = ").append(numCallsToLambda).append("\n");
		buffer.append("timeUsedByLambda = ").append(timeUsedByLambda).append(" ms\n");
		buffer.append("numCallsToClear = ").append(numCallsToClear).append("\n");
		buffer.append("timeUsedByClear = ").append(timeUsedByClear).append(" ms\n");
		buffer.append("numCallsToInitialize = ").append(numCallsToInitialize).append("\n");
		buffer.append("timeUsedByInitialize = ").append(timeUsedByInitialize).append(" ms\n");
		buffer.append("numCallsToGetTN = ").append(numCallsToGetTN).append("\n");
		buffer.append("timeUsedByGetTN = ").append(timeUsedByGetTN).append(" ms\n");
		buffer.append("numCallsToGetTL = ").append(numCallsToGetTL).append("\n");
		buffer.append("timeUsedByGetTL = ").append(timeUsedByGetTL).append(" ms\n");
		buffer.append("numCallsToSetTN = ").append(numCallsToSetTN).append("\n");
		buffer.append("timeUsedBySetTN = ").append(timeUsedBySetTN).append(" ms\n");
		buffer.append("numCallsToSetTL = ").append(numCallsToSetTL).append("\n");
		buffer.append("timeUsedBySetTL = ").append(timeUsedBySetTL).append(" ms\n");
		buffer.append("numCallsToGetClock = ").append(numCallsToGetClock).append("\n");
		buffer.append("timeUsedByGetClock = ").append(timeUsedByGetClock).append(" ms\n");
		return buffer.toString();
	}

}
