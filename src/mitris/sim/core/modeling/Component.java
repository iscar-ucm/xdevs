package mitris.sim.core.modeling;

import java.util.Collection;

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
	public DevsCoupled getParent();
	public void setParent(DevsCoupled parent);
	public void initialize();	
}
