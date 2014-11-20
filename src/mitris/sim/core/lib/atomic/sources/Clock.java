package mitris.sim.core.lib.atomic.sources;

import mitris.sim.core.atomic.sinks.Console;
import mitris.sim.core.modeling.DevsAtomic;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Port;
import mitris.sim.core.simulation.Coordinator;

/**
 *
 * @author José Luis Risco Martín
 */
public class Clock extends DevsAtomic {

    public Port<Object> iStop = new Port<>("iStop");
    public Port<Integer> oClk = new Port<>("oClk");
    protected double period;
    protected double semiPeriod;
    protected int nextValue;
    protected long count;

    public Clock(String name, double period, int initialValue) {
    	super(name);
        super.addInPort(iStop);
        super.addOutPort(oClk);
        this.period = period;
        this.semiPeriod = period/2.0;
        this.nextValue = 0;
        this.count = 0L;
        if (initialValue != 0) {
            this.nextValue = 1;
        }
        super.activate();
    }

    public Clock(String name, double period) {
        this(name, period, 1);
    }

    public Clock(String name) {
        this(name, 1, 1);
    }

    @Override
    public void deltint() {
        // New semiperiod:
        count++;
        // New Value:
        nextValue = 1 - nextValue;
        // Nex value in a semiperiod:
        super.holdIn("active", semiPeriod);
    }

    @Override
    public void deltext(double e) {
        if (!iStop.isEmpty()) {
            super.passivate();
        }
    }

    @Override
    public void lambda() {
        oClk.addValue(nextValue);
    }

    public long getCount() {
        return count;
    }

    public static void main(String[] args) {
        Coupled clockExample = new Coupled("clockExample");
        Clock clock = new Clock("clock",10.0);
        clockExample.addComponent(clock);
        Console console = new Console("console");
        clockExample.addComponent(console);
        clockExample.addCoupling(clock, clock.oClk, console, console.iIn);
        Coordinator coordinator = new Coordinator(clockExample);
        coordinator.simulate(30.0);
    }
}
