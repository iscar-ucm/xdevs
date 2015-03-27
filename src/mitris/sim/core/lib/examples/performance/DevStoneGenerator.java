package mitris.sim.core.lib.examples.performance;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.OutPort;

/**
 * Events generator for the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 */
public class DevStoneGenerator extends Atomic {

    public OutPort<Long> oOut = new OutPort<>("out");
    protected double preparationTime;
    protected double period;
    protected long counter = 1;
    protected long maxEvents = Long.MAX_VALUE;

    public DevStoneGenerator(String name, DevStoneProperties properties, long maxEvents) {
        super(name);
        super.addOutPort(oOut);
        this.preparationTime = properties.getPropertyAsDouble(DevStoneProperties.PREPARATION_TIME);
        this.period = properties.getPropertyAsDouble(DevStoneProperties.GENERATOR_PERIOD);
        this.maxEvents = (maxEvents <= 0) ? 1 : maxEvents;
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
