package xdevs.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

import xdevs.core.modeling.api.ComponentInterface;
import xdevs.core.modeling.api.PortInterface;

/**
 * @author smittal
 *
 */
public class Port<E> extends Entity implements PortInterface<E> {

    protected ComponentInterface parent;
    protected LinkedList<E> values = new LinkedList<>();

    public Port(String name) {
        super(name);
    }

    public Port() {
        this(PortInterface.class.getSimpleName());
    }

	// Entity members
    @Override
    public String toString() {
        return getQualifiedName();
    }

	// Port members
    @Override
    public void clear() {
        //values = new LinkedList<>();
        values.clear(); // <- What choice has a better performance?
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
        for(E value : values)
            this.values.add(value);
    }

    @Override
    public ComponentInterface getParent() {
        return parent;
    }

    @Override
    public String getQualifiedName() {
        if (parent == null) {
            return name;
        }
        return parent.getName() + "." + name;
    }
}
