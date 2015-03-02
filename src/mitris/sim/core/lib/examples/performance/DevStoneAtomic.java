package mitris.sim.core.lib.examples.performance;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.util.Dhrystone;

/**
 *
 * @author José Luis Risco Martín TODO: I must also modify this class, according
 * to the source code implemented by Saurabh, a iStart input port must be added.
 */
public class DevStoneAtomic extends Atomic {
    
    public InPort<Object> in = new InPort<>("in");
    public OutPort<Object> out = new OutPort<>("out");
    protected Dhrystone dhrystone;
    
    protected double preparationTime;
    protected double intDelayTime;
    protected double extDelayTime;
    
    public DevStoneAtomic(String name, double preparationTime, double intDelayTime, double extDelayTime) {
        super(name);
        super.addInPort(in);
        super.addOutPort(out);
        this.preparationTime = preparationTime;
        this.intDelayTime = intDelayTime;
        this.extDelayTime = extDelayTime;        
    }
    
    @Override
    public void initialize() {
        super.holdIn("active", preparationTime);
    }
    
    @Override
    public void deltint() {
        Dhrystone.execute(intDelayTime, false);
        super.passivate();
    }
    
    @Override
    public void deltext(double e) {
        Dhrystone.execute(extDelayTime, false);
        super.holdIn("active", preparationTime);
    }
    
    @Override
    public void lambda() {
        out.addValue(1);
    }
}
