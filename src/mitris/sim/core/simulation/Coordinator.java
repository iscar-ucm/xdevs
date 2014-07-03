package mitris.sim.core.simulation;

import java.util.Collection;
import java.util.LinkedList;

import mitris.sim.core.Constants;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Component;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Coupling;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Coordinator extends AbstractSimulator {

    protected Coupled model;
    protected LinkedList<AbstractSimulator> simulators = new LinkedList<>();

    public Coordinator(SimulationClock clock, Coupled model, boolean flatten) {
        super(clock);
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
            } else if (component instanceof Atomic) {
                Simulator simulator = new Simulator(clock, (Atomic) component);
                simulators.add(simulator);
            }
        }
        tL = clock.getTime();
        tN = tL + ta();
    } 
    
    public Coordinator(Coupled model, boolean flatten) {
        this(new SimulationClock(), model, flatten);
    }

    public Coordinator(Coupled model) {
        this(model, true);
    }

    @Override
    public final double ta() {
        double tn = Constants.INFINITY;
        for (AbstractSimulator simulator : simulators) {
            if (simulator.tN < tn) {
                tn = simulator.tN;
            }
        }
        return tn - clock.getTime();
    }

    @Override
    public void lambda() {
        for (AbstractSimulator simulator : simulators) {
            simulator.lambda();
        }
        propagateOutput();
    }

    public void propagateOutput() {
        LinkedList<Coupling> ic = model.getIC();
        for (Coupling c : ic) {
            c.propagateValues();
        }

        LinkedList<Coupling> eoc = model.getEOC();
        for (Coupling c : eoc) {
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
        LinkedList<Coupling> eic = model.getEIC();
        for (Coupling c : eic) {
            c.propagateValues();
        }
    }

    @Override
    public void clear() {
        for (AbstractSimulator simulator : simulators) {
            simulator.clear();
        }
        Collection<Port> ports;
        ports = model.getInPorts();
        for (Port port : ports) {
            port.clear();
        }
        ports = model.getOutPorts();
        for (Port port : ports) {
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
