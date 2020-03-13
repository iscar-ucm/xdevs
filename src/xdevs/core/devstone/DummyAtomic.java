package xdevs.core.devstone;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

import java.util.LinkedList;
import java.util.List;

import static xdevs.core.util.Constants.INFINITY;

public class DummyAtomic extends Atomic {

    private double intDelay, extDelay;
    private double prepTime;
    private List<Integer> values;
    protected Port<Integer> iIn, oOut;

    public DummyAtomic(String name, double intDelay, double extDelay) {
        this(name, intDelay, extDelay, true, 0);
    }

    public DummyAtomic(String name, double intDelay, double extDelay, boolean addOutPort) {
        this(name, intDelay, extDelay, addOutPort, 0);
    }

    public DummyAtomic(String name, double intDelay, double extDelay, boolean addOutPort, double prepTime) {
        super(name);
        this.intDelay = intDelay;
        this.extDelay = extDelay;
        this.prepTime = prepTime;
        this.values = new LinkedList<>();

        this.iIn = new Port<>("iIn");
        this.addInPort(this.iIn);

        if(addOutPort) {
            this.oOut = new Port<>("oOut");
            this.addOutPort(this.oOut);
        }
    }

    @Override
    public void deltint() {
        // TODO: Add Dhrystone
        //if(this.intDelay != 0) {
        //    // Dhrystone code
        //}

        this.passivate();
    }

    @Override
    public void deltext(double e) {
        // TODO: Add Dhrystone
        //if(this.extDelay != 0) {
        //    // Dhrystone code
        //}
        if (this.sigma == INFINITY) {
            this.holdIn("active", prepTime);
        }
    }

    @Override
    public void lambda() {
        if(this.oOut != null) {
            this.oOut.addValue(0);
        }
    }

    @Override
    public void initialize() {
        this.passivate();
    }

    @Override
    public void exit() {

    }
}
