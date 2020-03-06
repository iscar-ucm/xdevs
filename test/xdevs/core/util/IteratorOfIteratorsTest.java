package xdevs.core.util;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IteratorOfIteratorsTest {
    @Test
    void testTwoIterators() {
        List<Integer> list1 = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            list1.add(i);
        }
        List<Integer> list2 = new LinkedList<>();
        for (int i = 10; i < 20; i++) {
            list2.add(i);
        }
        List<Iterator<Integer>> trial = new LinkedList<>();
        trial.add(list1.iterator());
        trial.add(list2.iterator());
        IteratorOfIterators<Integer> iteriterator = new IteratorOfIterators<>(trial);


        assertEquals(20, iteratorSize(iteriterator));
    }

    @Test
    void testSameIterator() {
        List<Integer> list1 = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            list1.add(i);
        }
        List<Iterator<Integer>> trial = new LinkedList<>();
        Iterator<Integer> iterator = list1.iterator();
        trial.add(iterator);
        trial.add(iterator);

        IteratorOfIterators<Integer> iteriterator = new IteratorOfIterators<>(trial);

        assertEquals(10, iteratorSize(iteriterator));
    }

    @Test
    void testEmptyIterator() {
        List<Iterator<Integer>> trial = new LinkedList<>();
        IteratorOfIterators<Integer> iteriterator = new IteratorOfIterators<>(trial);
        assertEquals(0, iteratorSize(iteriterator));
    }

    private int iteratorSize(Iterator it) {
        int nTimes = 0;
        while (it.hasNext()) {
            it.next();
            nTimes++;
        }
        return nTimes;
    }
}