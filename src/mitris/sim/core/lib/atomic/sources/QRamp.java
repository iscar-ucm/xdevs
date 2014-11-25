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
public class QRamp extends Atomic {

    public OutPort<Double> portOut = new OutPort<>("portOut");
    protected double startTime;
    protected double slope;
    protected double nextOutput;
    protected double qOutput;

    public QRamp(String name, double initialOutput, double startTime, double slope, double qOutput) {
    	super(name);
        super.addOutPort(portOut);
        this.nextOutput = initialOutput;
        this.startTime = startTime;
        this.slope = slope;
        this.qOutput = qOutput;
    }
    
    public void initialize() {
        super.holdIn("initialOutput", 0.0);    	
    }

    @Override
    public void deltint() {
        if (super.phaseIs("initialOutput")) {
            super.holdIn("startTime", startTime);
        } else {
            double sampleTime = qOutput / Math.abs(slope);
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
        QRamp qramp = new QRamp("qramp", 2, 10, 2, 0.1);
        example.addComponent(qramp);
        Console console = new Console("console");
        example.addComponent(console);
        example.addCoupling(qramp, qramp.portOut, console, console.iIn);
        Coordinator coordinator = new Coordinator(example);
        coordinator.simulate(30.0);
    }
}
