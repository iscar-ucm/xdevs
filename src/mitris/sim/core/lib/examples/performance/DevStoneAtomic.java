package mitris.sim.core.lib.examples.performance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.util.Dhrystone;

/**
 * Atomic model to study the performance using the DEVStone benchmark
 *
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
    
    public static long NUM_DELT_INTS = 0;
    public static long NUM_DELT_EXTS = 0;
    public static long NUM_OF_EVENTS = 0;
    
    public DevStoneAtomic(String name, DevStoneProperties properties) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
        this.preparationTime = properties.getPropertyAsDouble(DevStoneProperties.PREPARATION_TIME);
        this.intDelayTime = properties.getPropertyAsDouble(DevStoneProperties.INT_DELAY_TIME);
        this.extDelayTime = properties.getPropertyAsDouble(DevStoneProperties.INT_DELAY_TIME);
    }
    
    @Override
    public void initialize() {
        super.passivate();
    }
    
    @Override
    public void deltint() {
        NUM_DELT_INTS++;
        outValues.clear();
        Dhrystone.execute(intDelayTime);
        super.passivate();
    }
    
    @Override
    public void deltext(double e) {
        NUM_DELT_EXTS++;
        Dhrystone.execute(extDelayTime);
        if (!iIn.isEmpty()) {
            Collection<Integer> values = iIn.getValues();
            if(name.contains("L1_1_C1")) {
                logger.info(name + " -> Preparing " + values.size() + " events.");
            }
            NUM_OF_EVENTS += values.size();
            for (Integer value : values) {
                outValues.add(value);
            }
        }
        super.holdIn("active", preparationTime);
    }
    
    @Override
    public void lambda() {
        oOut.addValues(outValues);
    }
}
