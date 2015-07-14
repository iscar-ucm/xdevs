package xdevs.core.lib.examples.performance;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.OutPort;

/**
 * Events generator for the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 */
public class DevStoneGenerator extends Atomic {

    public OutPort<Integer> oOut = new OutPort<>("out");
    protected double preparationTime;
    protected double period;
    protected int counter = 1;
    protected int maxEvents = Integer.MAX_VALUE;

    public DevStoneGenerator(String name, DevStoneProperties properties, int maxEvents) {
        super(name);
        super.addOutPort(oOut);
        this.preparationTime = properties.getPropertyAsDouble(DevStoneProperties.PREPARATION_TIME);
        this.period = properties.getPropertyAsDouble(DevStoneProperties.GENERATOR_PERIOD);
        this.maxEvents = maxEvents;
    }

    @Override
    public void initialize() {
        counter = 1;
        this.holdIn("active", preparationTime);
    }

    @Override
    public void deltint() {
        counter++;
        if (counter > maxEvents) {
            super.passivate();
        } else {
            this.holdIn("active", period);
        }
    }

    @Override
    public void deltext(double e) {
        super.passivate();
    }

    @Override
    public void lambda() {
        oOut.addValue(counter);
    }
}