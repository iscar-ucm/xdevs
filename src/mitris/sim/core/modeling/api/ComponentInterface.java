package mitris.sim.core.modeling.api;

import java.util.Collection;

import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */
public interface ComponentInterface extends EntityInterface {

    public boolean isInputEmpty();

    public void addInPort(InPort<?> port);

    public Collection<InPort<?>> getInPorts();

    public void addOutPort(OutPort<?> port);

    public Collection<OutPort<?>> getOutPorts();

    public ComponentInterface getParent();

    public void setParent(ComponentInterface parent);

    public void initialize();
}
