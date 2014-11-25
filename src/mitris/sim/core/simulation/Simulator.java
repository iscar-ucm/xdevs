package mitris.sim.core.simulation;

import java.util.Collection;

import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.modeling.api.DevsAtomic;
import mitris.sim.core.simulation.api.SimulationClock;

/**
 *
 * @author José Luis Risco Martín
 */
public class Simulator extends AbstractSimulator {

    protected DevsAtomic model;
    
    public Simulator(SimulationClock clock, DevsAtomic model) {
        super(clock);
        this.model = model;
    }
    
    public void initialize() {
    	model.initialize();
        tL = clock.getTime();
        tN = tL + model.ta();    	
    }
    
   public double ta() {
        return model.ta();
    }

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

    public void lambda() {
        if (clock.getTime() == tN) {
            model.lambda();
        }
    }

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
    
    public DevsAtomic getModel() {
    	return model;
    }
    
}
