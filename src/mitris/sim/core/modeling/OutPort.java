package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */

public class OutPort<E> implements Port<E> {

	protected String name;
	protected Component parent;
	protected LinkedList<E> values = new LinkedList<>();
	
	public OutPort(String name) {
		this.name = name;
	}
	
	public OutPort() {
		this(OutPort.class.getSimpleName());
	}
	
	public String getName() {
		return name;
	}

	public void clear() {
		values.clear();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}

	public E getSingleValue() {
		return values.element();
	}

	public Collection<E> getValues() {
		return values;
	}

	public void addValue(E value) {
		values.add(value);
	}

	public void addValues(Collection<E> values) {
		this.values.addAll(values);
	}	
	
	public Component getParent() {
		return parent;
	}
}
