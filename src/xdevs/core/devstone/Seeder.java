package xdevs.core.devstone;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

public class Seeder extends Atomic {

    protected Port<Integer> oOut;
    protected int nMessages;

    public Seeder(String name, int nMessages) {
        super(name);
        this.nMessages = nMessages;
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
        for (int i = 0; i < this.nMessages; i++) {
            this.oOut.addValue(i);
        }
    }

    @Override
    public void initialize() {
        this.activate();
    }

    @Override
    public void exit() { }
}
