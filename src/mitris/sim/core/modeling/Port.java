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
public class Port<E> extends Entity {

	protected Component isPartOf;
	protected LinkedList<E> values = new LinkedList<>();
	
	public Port(String name) {
		super(name);
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
}
