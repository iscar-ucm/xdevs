/**
 * 
 */
package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

import mitris.sim.core.modeling.api.Component;
import mitris.sim.core.modeling.api.Port;

/**
 * @author smittal
 *
 */
public class PortBase<E> implements Port<E> {

	protected String name;
	protected Component parent;
	protected LinkedList<E> values = new LinkedList<>();
	
	public PortBase(String name) {
		this.name = name;
	}
	
	public PortBase() {
		this(Port.class.getSimpleName());
	}
	
	// Entity members
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	// Port members

	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public E getSingleValue() {
		return values.element();
	}

	@Override
	public Collection<E> getValues() {
		return values;
	}

	@Override
	public void addValue(E value) {
		values.add(value);
	}

	@Override
	public void addValues(Collection<E> values) {
		this.values.addAll(values);
	}	
	
	@Override
	public Component getParent() {
		return parent;
	}

	@Override
	public String getQualifiedName() {
		return name;
	}
}
