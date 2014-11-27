/**
 * 
 */
package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

import mitris.sim.core.modeling.api.Component;
import mitris.sim.core.modeling.api.DevsCoupled;

/**
 * @author smittal
 *
 */
public abstract class ComponentBase extends EntityBase implements Component {

	// Component attributes
	protected Component parent = null;
	protected LinkedList<InPort<?>> inPorts = new LinkedList<>();
	protected LinkedList<OutPort<?>> outPorts = new LinkedList<>();

	public ComponentBase(){
		this(ComponentBase.class.getSimpleName());
	}
	
	public ComponentBase(String name){
		super(name);
	}

	public boolean isInputEmpty() {
		for (InPort<?> port : inPorts) {
			if (!port.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public void addInPort(InPort<?> port) {
		inPorts.add(port);
		port.parent = this;
	}

	public Collection<InPort<?>> getInPorts() {
		return inPorts;
	}

	public void addOutPort(OutPort<?> port) {
		outPorts.add(port);
		port.parent = this;
	}

	public Collection<OutPort<?>> getOutPorts() {
		return outPorts;
	}

	@Override
	public Component getParent() {
		return parent;
	}

	@Override
	public void setParent(Component parent) {
		this.parent = parent;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder(name + " :");
		sb.append(" Inports[ ");
		for(InPort<?> p : inPorts){
			sb.append(p + " ");
		}
		sb.append("]");
		sb.append(" Outports[ ");
		for(OutPort<?> p: outPorts){
			sb.append(p+" ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	@Override
	public String getQualifiedName() {
		if (parent == null) {
			return name;
		}
		return parent.getQualifiedName() + "." + name;
	}

}
