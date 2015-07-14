package xdevs.core.modeling;

import xdevs.core.modeling.api.PortInterface;


/**
 * TODO: Saurabh wants and interface for Coupling. I find no reason to create such interface. There is only
 * one kind of Coupling (for now), so no interfaces are needed.
 * @author José Luis Risco Martín
 */
public class Coupling<E> {

	protected PortInterface<E> portFrom;
	protected PortInterface<E> portTo;

	public Coupling(PortInterface<E> portFrom, PortInterface<E> portTo) {
		this.portFrom = portFrom;
		this.portTo = portTo;
	}

	public String toString(){
		return "(" + portFrom + "->" + portTo+")";
	}

	// Coupling members
	
	public void propagateValues() {
		portTo.addValues(portFrom.getValues());
	}

	public PortInterface<E> getPortFrom() {
		return portFrom;
	}

	public PortInterface<E> getPortTo() {
		return portTo;
	}
}
