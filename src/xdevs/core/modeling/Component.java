/**
 * 
 */
package xdevs.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

import xdevs.core.modeling.api.ComponentInterface;
import xdevs.core.modeling.api.CoupledInterface;

/**
 * @author smittal
 *
 */
public abstract class Component extends Entity implements ComponentInterface {

	// Component attributes
	protected ComponentInterface parent = null;
	protected LinkedList<InPort<?>> inPorts = new LinkedList<>();
	protected LinkedList<OutPort<?>> outPorts = new LinkedList<>();

	public Component(){
		this(Component.class.getSimpleName());
	}
	
	public Component(String name){
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
	public ComponentInterface getParent() {
		return parent;
	}

	@Override
	public void setParent(ComponentInterface parent) {
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
