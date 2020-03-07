package xdevs.core.devstone;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

public class Seeder extends Atomic {

    protected Port<Integer> oOut;

    public Seeder(String name) {
        super(name);
        this.oOut = new Port<>("oOut");
        this.addOutPort(this.oOut);
    }

    @Override
    public void deltint() {
        this.passivate();
    }

    @Override
    public void deltext(double e) { }

    @Override
    public void lambda() {
        this.oOut.addValue(0);
    }

    @Override
    public void initialize() {
        this.activate();
    }

    @Override
    public void exit() { }
}
