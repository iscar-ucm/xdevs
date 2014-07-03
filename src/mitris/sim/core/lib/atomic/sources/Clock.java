package mitris.sim.core.lib.atomic.sources;

import mitris.sim.core.atomic.sinks.Console;
import mitris.sim.core.atomic.sinks.Disk;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Port;
import mitris.sim.core.simulation.Coordinator;
import mitris.sim.core.simulation.SimulationClock;

/**
 *
 * @author jlrisco
 */
public class Clock extends Atomic {

    public Port<Object> iStop = new Port<>();
    public Port<Integer> oClk = new Port<>();
    protected double period;
    protected double semiPeriod;
    protected int nextValue;
    protected long count;

    public Clock(double period, int initialValue) {
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

    public Clock(double period) {
        this(period, 1);
    }

    public Clock() {
        this(1, 1);
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
        Coupled clockExample = new Coupled();
        Clock clock = new Clock(10.0);
        clockExample.addComponent(clock);
        Console console = new Console();
        clockExample.addComponent(console);
        clockExample.addCoupling(clock, clock.oClk, console, console.iIn);
        Coordinator coordinator = new Coordinator(clockExample);
        coordinator.simulate(30.0);
    }
}
