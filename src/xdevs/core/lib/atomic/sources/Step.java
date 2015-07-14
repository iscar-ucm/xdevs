package xdevs.core.lib.atomic.sources;

import xdevs.core.atomic.sinks.Console;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.OutPort;
import xdevs.core.simulation.Coordinator;

/**
 *
 * @author José Luis Risco Martín
 */
public class Step extends Atomic {

    public OutPort<Double> portOut = new OutPort<Double>("portOut");
    protected double initialValue;
    protected double stepTime;
    protected double finalValue;

    public Step(String name, double initialValue, double stepTime, double finalValue) {
    	super(name);
        super.addOutPort(portOut);
        this.initialValue = initialValue;
        this.stepTime = stepTime;
        this.finalValue = finalValue;
    }
    
    public void initialize() {
        super.holdIn("initialValue", 0.0);    	
    }

    @Override
    public void deltint() {
        if (super.phaseIs("initialValue")) {
            super.holdIn("finalValue", stepTime);
        } else if (super.phaseIs("finalValue")) {
            super.passivate();
        }
    }

    @Override
    public void deltext(double e) {
    }

    @Override
    public void lambda() {
        if (super.phaseIs("initialValue")) {
            portOut.addValue(initialValue);
        } else if (super.phaseIs("finalValue")) {
            portOut.addValue(finalValue);
        }
    }

    public static void main(String[] args) {
        Coupled stepExample = new Coupled("stepExample");
        Step step = new Step("step", 0, 15, 10);
        stepExample.addComponent(step);
        Console console = new Console("console");
        stepExample.addComponent(console);
        stepExample.addCoupling(step, step.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(stepExample);
        coordinator.simulate(30.0);
    }
}
