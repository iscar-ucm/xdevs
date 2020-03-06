package xdevs.core.util;

import java.util.Iterator;
import java.util.List;


public class IteratorOfIterators<E> implements Iterator<E> {
    private final List<Iterator<E>> iterators;
    private int current = 0;

    public IteratorOfIterators(List<Iterator<E>> iterators) {
        this.iterators = iterators;
    }

    public boolean hasNext() {
        while (current <  iterators.size() && !iterators.get(current).hasNext()) {
            current++;
        }
        return current < iterators.size();
    }

    public E next() {
        while (current < iterators.size() && !iterators.get(current).hasNext()) {
            current++;
        }
        return iterators.get(current).next();
    }

    /**
     * This method is not used
     */
    public void remove() {
         iterators.clear();
         current = 0;
    }
}
