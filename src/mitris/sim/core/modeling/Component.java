package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */
@SuppressWarnings("rawtypes")
public abstract class Component extends Entity {

	protected LinkedList<Port> inPorts = new LinkedList<>();
	protected LinkedList<Port> outPorts = new LinkedList<>();

	public Component(String name) {
		super(name);
	}

	public boolean isInputEmpty() {
		for (Port port : inPorts) {
			if (!port.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void addInPort(Port port) {
		inPorts.add(port);
		port.partOf = this;
	}

	public Collection<Port> getInPorts() {
		return inPorts;
	}

	@SuppressWarnings("unchecked")
	public void addOutPort(Port port) {
		outPorts.add(port);
		port.partOf = this;
	}

	public Collection<Port> getOutPorts() {
		return outPorts;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder(name + " :");
		sb.append(" Inports[ ");
		for(Port p : inPorts){
			sb.append(p + " ");
		}
		sb.append("]");
		sb.append(" Outports[ ");
		for(Port p: outPorts){
			sb.append(p+" ");
		}
		sb.append("]");
		return sb.toString();
	}
}
