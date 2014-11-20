package mitris.sim.core.modeling;


/**
 *
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
