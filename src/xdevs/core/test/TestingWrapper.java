
package xdevs.core.test;

import xdevs.core.modeling.Atomic;
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

    public void addTransducer(Port<?> port, Transducer<?> transducer) {
        //addCoupling(port, buffer.iIn);
        //addComponent(buffer);

        Coupled comp = (Coupled) port.getParent().getParent();
        comp.addComponent(transducer);
        comp.addCoupling(port, transducer.iIn);
    }
}
