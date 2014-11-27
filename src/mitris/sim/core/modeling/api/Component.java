package mitris.sim.core.modeling.api;

import java.util.Collection;

import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */
public interface Component extends Entity {
	public boolean isInputEmpty();
	public void addInPort(InPort<?> port);
	public Collection<InPort<?>> getInPorts();
	public void addOutPort(OutPort<?> port);
	public Collection<OutPort<?>> getOutPorts();
	public Component getParent();
	public void setParent(Component parent);
	public void initialize();	
}
