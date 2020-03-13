package xdevs.core.devstone;

import org.junit.jupiter.api.Test;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public abstract class DevstoneUtilsTestCase {

    protected final LinkedList<int[]> validHighParams;
    protected final LinkedList<int[]> validLowParams;

    public DevstoneUtilsTestCase() {
        this(10);
    }

    public DevstoneUtilsTestCase(int numValidParamsSet) {
        this.validHighParams = new LinkedList<int[]>();
        this.validLowParams = new LinkedList<int[]>();

        Random r = new Random();

        for (int i = 0; i < numValidParamsSet; i++) {
            validHighParams.add(new int[]{1 + r.nextInt(100), 1 + r.nextInt(200), 1 + r.nextInt(1000), 1 + r.nextInt(1000)});
            validLowParams.add(new int[]{1 + r.nextInt(20), 1 + r.nextInt(30), 1 + r.nextInt(10), 1 + r.nextInt(10)});
        }

    }

    protected void checkInvalidInputs(Class<?> cls) throws NoSuchMethodException {
        Constructor<?> cons = cls.getConstructor(String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 0, 1, 1, 1));
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 1, 0, 1, 1));
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 1, 1, -1, 0));
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 1, 1, 0, -1));
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 0, 1, -1, -1));
        assertThrows(InvocationTargetException.class, () -> cons.newInstance("root", 0, 0, -1, -1));
    }

    protected void testBehaviorSequential() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        for (int[] params : this.validLowParams) {
            testBehavior(Coordinator.class, params[0], params[1], params[2], params[3]);
        }
    }

    protected abstract void testBehavior(Class<?> coordinator, int depth, int width, int int_cycles, int ext_cycles) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;
    protected abstract void testInvalidInputs() throws NoSuchMethodException;

    public static int countAtomics(Coupled root) {
        int count = 0;
        for (Component component : root.getComponents()) {
            if(component instanceof Atomic) {
                count += 1;
            } else if (component instanceof Coupled) {
                count += countAtomics((Coupled) component);
            } else {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }

    public static int countIC(Coupled root) {
        int count = root.getIC().size();
        for (Component component : root.getComponents()) {
            if (component instanceof Coupled) {
                count += countIC((Coupled) component);
            } else if (!(component instanceof Atomic)) {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }

    public static int countEIC(Coupled root) {
        int count = root.getEIC().size();
        for (Component component : root.getComponents()) {
            if (component instanceof Coupled) {
                count += countEIC((Coupled) component);
            } else if (!(component instanceof Atomic)) {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }

    public static int countEOC(Coupled root) {
        int count = root.getEOC().size();
        for (Component component : root.getComponents()) {
            if (component instanceof Coupled) {
                count += countEOC((Coupled) component);
            } else if (!(component instanceof Atomic)) {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }

    public static int countInternalTransitions(Coupled root) {
        int count = 0;
        for (Component component : root.getComponents()) {
            if(component instanceof Atomic) {
                DummyAtomicStats atomic = (DummyAtomicStats) component;
                count += atomic.getIntCount();
            } else if (component instanceof Coupled) {
                count += countInternalTransitions((Coupled) component);
            } else {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }

    public static int countExternalTransitions(Coupled root) {
        int count = 0;
        for (Component component : root.getComponents()) {
            if(component instanceof Atomic) {
                DummyAtomicStats atomic = (DummyAtomicStats) component;
                count += atomic.getIntCount();
            } else if (component instanceof Coupled) {
                count += countExternalTransitions((Coupled) component);
            } else {
                throw new RuntimeException("Unrecognized component type.");
            }
        }
        return count;
    }
}
