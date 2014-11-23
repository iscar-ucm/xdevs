/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling.api;

import java.util.Collection;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */

public interface Port<E> extends Entity {
	public void clear();
	public boolean isEmpty();
	public E getSingleValue();
	public Collection<E> getValues();
	public void addValue(E value);
	public void addValues(Collection<E> values);
	public Component getParent();
}
