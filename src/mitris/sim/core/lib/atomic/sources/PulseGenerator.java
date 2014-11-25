/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.atomic.sources;

import mitris.sim.core.atomic.sinks.Console;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.OutPort;
import mitris.sim.core.simulation.Coordinator;

/**
 *
 * @author jlrisco
 */
public class PulseGenerator extends Atomic {

    public OutPort<Double> portOut = new OutPort<>("portOut");
    protected double amplitude;
    protected double pulseWidth;
    protected double period;
    protected double phaseDelay;

    public PulseGenerator(String name, double amplitude, double pulseWidth, double period, double phaseDelay) {
    	super(name);
        super.addOutPort(portOut);
        this.amplitude = amplitude;
        this.pulseWidth = pulseWidth;
        this.period = period;
        this.phaseDelay = phaseDelay;
    }
    
    public void initialize() {
        super.holdIn("delay", 0);    	
    }

    @Override
    public void deltint() {
        if (super.phaseIs("delay")) {
            super.holdIn("high", phaseDelay);
        } else if (super.phaseIs("high")) {
            super.holdIn("low", pulseWidth);
        } else if (super.phaseIs("low")) {
            super.holdIn("high", period - pulseWidth);
        }
    }

    @Override
    public void deltext(double e) {
    }

    @Override
    public void lambda() {
        if (super.phaseIs("delay")) {
            portOut.addValue(0.0);
        } else if (super.phaseIs("high")) {
            portOut.addValue(amplitude);
        } else if (super.phaseIs("low")) {
            portOut.addValue(0.0);
        }
    }

    public static void main(String[] args) {
        Coupled pulseExample = new Coupled("pulseExample");
        PulseGenerator pulse = new PulseGenerator("pulse",10, 3, 5, 5);
        pulseExample.addComponent(pulse);
        Console console = new Console("console");
        pulseExample.addComponent(console);
        pulseExample.addCoupling(pulse, pulse.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(pulseExample);
        coordinator.simulate(30.0);
    }
}
