/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
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
