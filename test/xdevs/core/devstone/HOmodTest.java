package xdevs.core.devstone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;


class HOmodTest extends DevstoneUtilsTestCase {

    @Test
    void testStructure() {
        for (int[] params : this.validHighParams) {
            testStructure(params[0], params[1], params[2], params[3]);
        }
    }

    void testStructure(int depth, int width, int int_cycles, int ext_cycles) {
        HOmod HOmodRoot = new HOmod("HOmod_root", depth, width, int_cycles, ext_cycles);

        // ICs relative to the "triangular" section
        int expectedIC = 0;
        if(width > 1) {
            expectedIC = (((width - 2) * (width - 1)) / 2);
        }

        // Plus the ones relative to the connection from the 2nd to 1st row...
        expectedIC += Math.pow((width - 1), 2);
        // ...and from the 1st to the couple component
        expectedIC += width - 1;
        // Multiplied by the number of layers (except the deepest one, that doesn't have ICs)
        expectedIC *= (depth - 1);

        assertEquals(((width - 1) + ((width - 1) * width) / 2) * (depth - 1) + 1, DevstoneUtilsTestCase.countAtomics(HOmodRoot));
        assertEquals(expectedIC, DevstoneUtilsTestCase.countIC(HOmodRoot));
        assertEquals((2*(width - 1) + 1) * (depth - 1) + 1, DevstoneUtilsTestCase.countEIC(HOmodRoot));
        assertEquals(depth, DevstoneUtilsTestCase.countEOC(HOmodRoot));
    }

    @Test
    @Override
    protected void testBehaviorSequential() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        super.testBehaviorSequential();
    }

    @Override
    protected void testBehavior(Class<?> coordClass, int depth, int width, int int_cycles, int ext_cycles) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        depth = width = 3;
        DEVStone HOmodEnv = new DEVStone("root", "HOmod", depth, width, int_cycles, ext_cycles, true);
        Constructor<?> cons = coordClass.getConstructor(Coupled.class);
        Coordinator coordinator = (Coordinator) cons.newInstance(HOmodEnv);
        coordinator.initialize();
        coordinator.simulate(Double.MAX_VALUE);
        Coupled HOmodRoot = (Coupled) HOmodEnv.getComponentByName("root_homod");
        int intTrans = DevstoneUtilsTestCase.countInternalTransitions(HOmodRoot);
        int extTrans = DevstoneUtilsTestCase.countExternalTransitions(HOmodRoot);

        int expectedTrans = 1;
        int trAtomics = (width - 1) * width / 2;
        for (int i = 1; i < depth; i++) {
            int numInputs = 1 + (i - 1)*(width - 1);
            int transFirstRow = (width - 1) * (numInputs + width - 1);
            expectedTrans += numInputs * trAtomics + transFirstRow;
        }

        assertEquals(expectedTrans, intTrans);
        assertEquals(expectedTrans, extTrans);
    }


    @Test
    void testCornerCases() {
        this.testStructure(10, 1, 1, 1);
        this.testStructure(1, 1, 1, 1);
    }

    @Test
    @Override
    protected void testInvalidInputs() throws NoSuchMethodException {
        this.checkInvalidInputs(HOmod.class);
    }


}