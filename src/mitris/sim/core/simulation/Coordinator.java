package mitris.sim.core.simulation;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import mitris.sim.core.Constants;
import mitris.sim.core.modeling.Component;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Coupling;
import mitris.sim.core.modeling.DevsAtomic;
import mitris.sim.core.modeling.DevsCoupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.util.Util;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coordinator extends AbstractSimulator {

	private static final Logger logger = Logger.getLogger(Coordinator.class.getName());

	protected DevsCoupled model;
	protected LinkedList<AbstractSimulator> simulators = new LinkedList<>();

	public Coordinator(SimulationClock clock, Coupled model, boolean flatten) {
		super(clock);
		logger.fine("Hierarchical...\n" + Util.printCouplings(model));
		if(flatten) {
			this.model = model.flatten(null);
		}
		else {
			this.model = model;
		}
		// Build hierarchy
		Collection<Component> components = model.getComponents();
		for (Component component : components) {
			if (component instanceof Coupled) {
				Coordinator coordinator = new Coordinator(clock, (Coupled) component, false);
				simulators.add(coordinator);
			} else if (component instanceof DevsAtomic) {
				Simulator simulator = new Simulator(clock, (DevsAtomic) component);
				simulators.add(simulator);
			}
		}

		logger.fine("After flattening.....\n"+ Util.printCouplings(this.model));
		logger.fine(this.model.toString());
		Iterator<Component> itr  = this.model.getComponents().iterator();
		while(itr.hasNext()){
			logger.fine("Component: "+itr.next());
		}
		logger.fine("START SIMULATION");        

		tL = clock.getTime();
		tN = tL + ta();
	} 

	public Coordinator(Coupled model, boolean flatten) {
		this(new SimulationClock(), model, flatten);
	}

	public Coordinator(Coupled model) {
		this(model, true);
	}

	public final double ta() {
		double tn = Constants.INFINITY;
		for (AbstractSimulator simulator : simulators) {
			if (simulator.tN < tn) {
				tn = simulator.tN;
			}
		}
		return tn - clock.getTime();
	}

	public void lambda() {
		for (AbstractSimulator simulator : simulators) {
			simulator.lambda();
		}
		propagateOutput();
	}

	public void propagateOutput() {
		LinkedList<Coupling<?>> ic = model.getIC();
		for (Coupling<?> c : ic) {
			c.propagateValues();
		}

		LinkedList<Coupling<?>> eoc = model.getEOC();
		for (Coupling<?> c : eoc) {
			c.propagateValues();
		}
	}

	@Override
	public void deltfcn() {
		propagateInput();
		for (AbstractSimulator simulator : simulators) {
			simulator.deltfcn();
		}
		tL = clock.getTime();
		tN = tL + ta();
	}

	public void propagateInput() {
		LinkedList<Coupling<?>> eic = model.getEIC();
		for (Coupling<?> c : eic) {
			c.propagateValues();
		}
	}

	public void clear() {
		for (AbstractSimulator simulator : simulators) {
			simulator.clear();
		}
		Collection<InPort<?>> inPorts;
		inPorts = model.getInPorts();
		for (InPort<?> port : inPorts) {
			port.clear();
		}
		Collection<OutPort<?>> outPorts;
		outPorts = model.getOutPorts();
		for (OutPort<?> port : outPorts) {
			port.clear();
		}
	} 

	public void simulate(long numIterations) {
		clock.setTime(tN);
		long counter;
		for (counter = 1; counter < numIterations && clock.getTime() < Constants.INFINITY; counter++) {
			lambda();
			deltfcn();
			clear();
			clock.setTime(tN);
		}
	}

	public void simulate(double timeInterval) {
		clock.setTime(tN);
		double tF = clock.getTime() + timeInterval;
		while (clock.getTime() < Constants.INFINITY && clock.getTime() < tF) {
			lambda();
			deltfcn();
			clear();
			clock.setTime(tN);
		}
	}
}
