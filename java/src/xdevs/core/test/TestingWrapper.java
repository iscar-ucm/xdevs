
package xdevs.core.test;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

public class TestingWrapper extends Coupled {

    public TestingWrapper(Atomic model) {
        super("TestingWrapper");
        addComponent(model);
    }

    public TestingWrapper(Coupled model) {
        super("TestingWrapper");
        addComponent(model);
    }

    public void addBuffer(Port port, Buffer buffer) {
        //addCoupling(port, buffer.iIn);
        //addComponent(buffer);

        Coupled comp = (Coupled) port.getParent().getParent();
        comp.addComponent(buffer);
        comp.addCoupling(port, buffer.iIn);
    }
}
