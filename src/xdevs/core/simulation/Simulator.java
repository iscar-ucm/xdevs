package xdevs.core.simulation;

import java.util.Collection;

import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;
import xdevs.core.modeling.api.AtomicInterface;
import xdevs.core.simulation.api.SimulationClock;

/**
 *
 * @author José Luis Risco Martín
 */
public class Simulator extends AbstractSimulator {

    protected AtomicInterface model;

    public Simulator(SimulationClock clock, AtomicInterface model) {
        super(clock);
        this.model = model;
    }

    @Override
    public void initialize() {
        model.initialize();
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

    @Override
    public void clear() {
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

    @Override
    public AtomicInterface getModel() {
        return model;
    }

}
