package mitris.sim.core.modeling;

import mitris.sim.core.modeling.api.Entity;
import mitris.sim.core.modeling.api.Port;


/**
 * TODO: Saurabh wants and interface for Coupling. I find no reason to create such interface. There is only
 * one kind of Coupling (for now), so no interfaces are needed.
 * @author José Luis Risco Martín
 */
public class Coupling<E> implements Entity {

	protected Port<E> portFrom;
	protected Port<E> portTo;

	public Coupling(Port<E> portFrom, Port<E> portTo) {
		this.portFrom = portFrom;
		this.portTo = portTo;
	}
	
	// Entity members
	public String getName() {
		return "{" + portFrom.getName() + " -> " + portTo.getName() + "}"; 
	}

	public String toString(){
		return "(" + portFrom.getParent().getName() + "." + portFrom + "->" + portTo.getParent().getName() + "." + portTo + ")";
	}

	// Coupling members
	
	public void propagateValues() {
		portTo.addValues(portFrom.getValues());
	}

	public Port<E> getPortFrom() {
		return portFrom;
	}

	public Port<E> getPortTo() {
		return portTo;
	}
}
