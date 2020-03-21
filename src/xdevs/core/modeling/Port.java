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
 *  - José Luis Risco Martín <jlrisco@ucm.es>
 *  - Román Cárdenas Rodríguez <r.cardenas@upm.es>
 */
package xdevs.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

public class Port<E> {

    protected Component parent = null;
    protected String name;
    protected LinkedList<E> values = new LinkedList<>();

    public Port(String name) {
        this.name = name;
    }

    public Port() {
        this(Port.class.getSimpleName());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        Component parentAux = this.getParent();
        while (parentAux != null) {
            sb.insert(0, ".");
            sb.insert(0, parentAux.getName());
            parentAux = parentAux.getParent();
        }
        return sb.toString();
    }
}
