package mitris.sim.core.lib.examples.performance;

import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 * Coupled model to study the performance using DEVStone
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupled extends Coupled {    
    public InPort<Integer> iIn = new InPort<>("in");
    public OutPort<Integer> oOut = new OutPort<>("out");

    public DevStoneCoupled(String name) {
        super(name);
        addInPort(iIn);
        addOutPort(oOut);
    }
}
