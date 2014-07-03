/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation;

import java.util.Collection;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Simulator extends AbstractSimulator {

    protected Atomic model;
    
    public Simulator(SimulationClock clock, Atomic model) {
        super(clock);
        this.model = model;
        tL = clock.getTime();
        tN = tL + model.ta();
    }

    @Override
    public double ta() {
        return model.ta();
    }

    @Override
    public void deltfcn() {
        double t = clock.getTime();
        boolean isInputEmpty = model.isInputEmpty();
        if (isInputEmpty && t != tN) {
            return;
        } else if (!isInputEmpty && t == tN) {
            double e = t - tL;
            model.setSigma(model.getSigma() - e);
            model.deltcon(e);
        } else if (isInputEmpty && t == tN) {
            model.deltint();
        } else if (!isInputEmpty && t != tN) {
            double e = t - tL;
            model.setSigma(model.getSigma() - e);
            model.deltext(e);
        }
        tL = t;
        tN = tL + model.ta();
    }

    @Override
    public void lambda() {
        if (clock.getTime() == tN) {
            model.lambda();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void clear() {
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
    
}
