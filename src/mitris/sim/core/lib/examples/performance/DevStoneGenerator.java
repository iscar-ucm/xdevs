package mitris.sim.core.lib.examples.performance;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.OutPort;

/**
 * Events generator for the DEVStone benchmark
 *
 * @author José Luis Risco Martín
 */
public class DevStoneGenerator extends Atomic {
    public OutPort<Object> oOut = new OutPort<>("out");
    protected double preparationTime;
    protected double period;
    protected long counter = 0;
    protected long maxEvents = Long.MAX_VALUE;

    public DevStoneGenerator(String name, double preparationTime, double period, long maxEvents) {
        super(name);
        super.addOutPort(oOut);
        this.preparationTime = preparationTime;
        this.period = period;
        this.maxEvents = maxEvents;
    }

    @Override
    public void initialize() {
        counter = 0;
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
        oOut.addValue(1);
    }
}
