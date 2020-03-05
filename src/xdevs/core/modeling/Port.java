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
import java.util.NoSuchElementException;

public class Port<E> {
    public enum Direction {NA, IN, OUT, INOUT};

    protected Component parent = null;
    protected String name;
    protected Direction direction = Direction.NA;
    protected LinkedList<E> values = new LinkedList<>();

    protected Boolean chained = false;
    protected LinkedList<Coupling<E>> couplingsIn = null;
    protected LinkedList<Coupling<E>> couplingsOut = null;

    public Port(String name) {
        this.name = name;
    }

    public Port() {
        this(Port.class.getSimpleName());
    }
    
    public String getName() {
        return name;
    }

    public Direction getDirection() {
        return direction;
    }

    public void clear() {
        values.clear();
    }

    public boolean isEmpty() {
        try {
            getSingleValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public E getSingleValue() {
        if (!chained || direction == Direction.OUT) {
            return values.element();
        } else {
            for (Coupling<E> coup: couplingsIn) {
                try {
                    return coup.getPortFrom().getSingleValue();
                } catch (NoSuchElementException ignored) { }
            }
            throw new NoSuchElementException();
        }
    }

    public Collection<E> getValues() {
        if (!chained || direction == Direction.OUT) {
            return values;
        } else {
            LinkedList<E> vals = new LinkedList<>();
            for (Coupling<E> coup: couplingsIn) {
                vals.addAll(coup.getPortFrom().getValues());
            }
            return vals;
        }
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

    public void toChain() {
        chained = true;
        couplingsIn = new LinkedList<>();
        couplingsOut = new LinkedList<>();
    }

    @SuppressWarnings({"unchecked"})
    public void addCouplingIn(Coupling<?> coupling) throws Exception {
        if (coupling.getPortTo() != this) {
            throw new Exception("Coupling does not end in this port");
        }
        couplingsIn.add((Coupling<E>) coupling);
    }

    @SuppressWarnings({"unchecked"})
    public void addCouplingOut(Coupling<?> coupling) throws Exception {
        if (coupling.getPortFrom() != this) {
            throw new Exception("Coupling does not start in this port");
        }
        couplingsOut.add((Coupling<E>)coupling);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        Component parentAux = this.getParent();
        while(parentAux!=null) {
            sb.insert(0, ".");
            sb.insert(0, parentAux.getName());
            parentAux = parentAux.getParent();
        }
        return sb.toString();
    }
}
