package xdevs.core.devstone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;


class LITest extends DevstoneUtilsTestCase {

    @Test
    void testStructure() {
        testStructure(3, 3, 0 ,0);
        for (int[] params : this.validHighParams) {
            testStructure(params[0], params[1], params[2], params[3]);
        }
    }

    void testStructure(int depth, int width, int int_cycles, int ext_cycles) {
        LI LIRoot = new LI("LI_root", depth, width, int_cycles, ext_cycles);

        assertEquals((width - 1) * (depth - 1) + 1, DevstoneUtilsTestCase.countAtomics(LIRoot));
        assertEquals(0, DevstoneUtilsTestCase.countIC(LIRoot));
        assertEquals(width * (depth - 1) + 1, DevstoneUtilsTestCase.countEIC(LIRoot));
        assertEquals(depth, DevstoneUtilsTestCase.countEOC(LIRoot));
    }

    @Test
    @Override
    protected void testBehaviorSequential() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        super.testBehaviorSequential();
    }

    @Override
    protected void testBehavior(Class<?> coordClass, int depth, int width, int int_cycles, int ext_cycles) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        DEVStone LIEnv = new DEVStone("root", "LI", depth, width, int_cycles, ext_cycles, true);
        Constructor<?> cons = coordClass.getConstructor(Coupled.class);
        Coordinator coordinator = (Coordinator) cons.newInstance(LIEnv);
        coordinator.initialize();
        coordinator.simulate(Double.MAX_VALUE);
        Coupled LIRoot = (Coupled) LIEnv.getComponentByName("root_li");
        int intTrans = DevstoneUtilsTestCase.countInternalTransitions(LIRoot);
        int extTrans = DevstoneUtilsTestCase.countExternalTransitions(LIRoot);
        int expectedTrans = (width - 1) * (depth - 1) + 1;
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
        this.checkInvalidInputs(LI.class);
    }


}