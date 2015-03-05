package mitris.sim.core.lib.examples.performance;

import java.util.LinkedList;
import java.util.logging.Logger;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.util.Dhrystone;

/**
 * Atomic model to study the performance using the DEVStone benchmark
 * @author José Luis Risco Martín
 * 
 */
public class DevStoneAtomic extends Atomic {
    private static final Logger logger = Logger.getLogger(DevStoneAtomic.class.getName());
    
    public InPort<Integer> iIn = new InPort<>("in");
    public OutPort<Integer> oOut = new OutPort<>("out");
    protected LinkedList<Integer> outValues = new LinkedList<>();
    protected Dhrystone dhrystone;
    
    protected double preparationTime;
    protected double intDelayTime;
    protected double extDelayTime;
    
    public DevStoneAtomic(String name, double preparationTime, double intDelayTime, double extDelayTime) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
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
        outValues.clear();
        Dhrystone.execute(intDelayTime);
        super.passivate();
    }
    
    @Override
    public void deltext(double e) {
        Dhrystone.execute(extDelayTime);
        if(!iIn.isEmpty()) {
            outValues.addAll(iIn.getValues());
        }
        super.holdIn("active", preparationTime);
    }
    
    @Override
    public void lambda() {
        oOut.addValues(outValues);
    }
}
