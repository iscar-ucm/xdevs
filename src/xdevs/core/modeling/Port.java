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

import xdevs.core.util.IteratorOfIterators;

import java.util.*;


public class Port<E> {

    public enum Direction {NA, IN, OUT, INOUT};

    protected Component parent = null;
    protected String name;
    protected Direction direction = Direction.NA;
    protected LinkedList<E> values = new LinkedList<>();

    protected Boolean chained = false;
    protected LinkedList<Coupling<E>> couplingsIn = null;
    protected LinkedList<Coupling<E>> couplingsOut = null;

    protected LinkedList<E> valuesMulticast = null;  // to cache all the elements in chained simulator
    protected boolean multicasted = false;
    protected E singleValue = null;  // to cache the first element in chained simulator
    protected boolean singleValueValid = false;

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
        if (chained) {
            valuesMulticast.clear();
            multicasted = false;
            singleValue = null;
            singleValueValid = false;
        }
    }

    public boolean isEmpty() {
        try {
            getSingleValue();
            return false;
        } catch (NoSuchElementException | IndexOutOfBoundsException Ignored) {
            return true;
        }
    }

    public E getSingleValue() {
        if (!chained || direction == Direction.OUT) {
            return values.element();
        } else if (singleValueValid) {
            return singleValue;
        } else {
            singleValue = (multicasted)? valuesMulticast.element() : getValuesIterator().next();
            singleValueValid = true;
            return singleValue;
        }
    }

    public Collection<E> getValues() {
        if (!chained || direction == Direction.OUT) {
            return values;
        } else if (!multicasted) {
            fillMulticast();
        }
        return valuesMulticast;
    }

    private void fillMulticast() {
        for (Coupling<E> coup: couplingsIn) {
            valuesMulticast.addAll(coup.getPortFrom().getValues());
        }
        multicasted = true;
    }

    public Iterator<E> getValuesIterator() {
        if (!chained || direction == Direction.OUT) {
            return values.iterator();
        } else if (multicasted) {  // if multicasted, we can just return an iterator of the local copy
            return valuesMulticast.iterator();
        } else {  // Otherwise, we return the corresponding the iterator of iterators
            List<Iterator<E>> iterators= new ArrayList<>();
            for (Coupling<E> coup: couplingsIn) {
                Iterator<E> i = coup.getPortFrom().getValuesIterator();
                if (i.hasNext()) {
                    iterators.add(i);
                }
            }
            return new IteratorOfIterators<>(iterators);
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
        valuesMulticast = new LinkedList<>();
    }

    @SuppressWarnings({"unchecked"})
    public void addCouplingIn(Coupling<?> coupling) {
        if (coupling.getPortTo() != this) {
            throw new RuntimeException("Coupling does not end in this port");
        }
        couplingsIn.add((Coupling<E>) coupling);
    }

    @SuppressWarnings({"unchecked"})
    public void addCouplingOut(Coupling<?> coupling) {
        if (coupling.getPortFrom() != this) {
            throw new RuntimeException("Coupling does not start in this port");
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
