/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.atomic.sources;

import mitris.sim.core.atomic.sinks.Console;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Port;
import mitris.sim.core.simulation.Coordinator;

/**
 *
 * @author jlrisco
 */
public class Ramp extends Atomic {

    public Port<Double> portOut = new Port<>("portOut");
    protected double startTime;
    protected double slope;
    protected double sampleTime;
    protected double nextOutput;

    public Ramp(String name, double initialOutput, double startTime, double slope, double sampleTime) {
    	super(name);
        super.addOutPort(portOut);
        this.nextOutput = initialOutput;
        this.startTime = startTime;
        this.slope = slope;
        this.sampleTime = sampleTime;
        super.holdIn("initialOutput", 0.0);
    }

    @Override
    public void deltint() {
        if (super.phaseIs("initialOutput")) {
            super.holdIn("startTime", startTime);
        } else {
            nextOutput += slope * sampleTime;
            super.holdIn("active", sampleTime);
        }
    }

    @Override
    public void deltext(double e) {
    }

    @Override
    public void lambda() {
        portOut.addValue(nextOutput);
    }

    public static void main(String[] args) {
        Coupled example = new Coupled("example");
        Ramp ramp = new Ramp("Ramp",2, 10, 2, 0.1);
        example.addComponent(ramp);
        Console console = new Console("console");
        example.addComponent(console);
        example.addCoupling(ramp, ramp.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(example);
        coordinator.simulate(30.0);
    }
}
