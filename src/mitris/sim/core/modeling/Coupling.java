package mitris.sim.core.modeling;


/**
 *
 * @author jlrisco
 */
@SuppressWarnings("rawtypes")
public class Coupling {

	protected Port portFrom;
	protected Port portTo;

	public Coupling(Port portFrom, Port portTo) {
		this.portFrom = portFrom;
		this.portTo = portTo;
	}

	@SuppressWarnings("unchecked")
	public void propagateValues() {
		portTo.addValues(portFrom.getValues());
	}

	public Port getPortFrom() {
		return portFrom;
	}

	public Port getPortTo() {
		return portTo;
	}
}
