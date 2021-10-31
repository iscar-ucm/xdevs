package xdevs.core.test;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;

import java.util.LinkedList;

public class Transducer<E> extends Atomic {

    protected Port<E> iIn = new Port<>("iIn");
    protected LinkedList<E> values = new LinkedList<>();

    public Transducer(String name) {
        super(name);
        super.addInPort(iIn);
    }

    @Override
    public void deltint() {

    }

    @Override
    public void deltext(double e) {
        super.resume(e);
        if (!iIn.isEmpty()) {
            for(E value: iIn.getValues()) {
                values.add(value);
            }
        }
    }

    @Override
    public void lambda() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void exit() {

    }

    public void clear() {
        values.clear();
    }

    public LinkedList<E> getValues() {
        return values;
    }
}
