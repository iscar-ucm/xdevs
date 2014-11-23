package mitris.sim.core.modeling;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */

public class OutPort<E> extends PortBase<E> {

	public OutPort(String name) {
		this.name = name;
	}
	
	public OutPort() {
		this(OutPort.class.getSimpleName());
	}
	
}
