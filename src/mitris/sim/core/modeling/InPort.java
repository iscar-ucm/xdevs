/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */

public class InPort<E> implements Port<E> {

	protected String name;
	protected Component parent;
	protected LinkedList<E> values = new LinkedList<>();
	
	public InPort(String name) {
		this.name = name;
	}
	
	public InPort() {
		this(InPort.class.getSimpleName());
	}
	
	// Entity members
	
	public String getName() {
		return name;
	}
	
	// Port members

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
