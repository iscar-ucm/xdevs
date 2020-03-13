package xdevs.core.devstone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;


class HOTest extends DevstoneUtilsTestCase {

    @Test
    void testStructure() {
        for (int[] params : this.validHighParams) {
            testStructure(params[0], params[1], params[2], params[3]);
        }
    }

    void testStructure(int depth, int width, int int_cycles, int ext_cycles) {
        HO HORoot = new HO("HO_root", depth, width, int_cycles, ext_cycles);

        int expectedIC = 0;
        if(width > 2) {
            expectedIC = (width - 2) * (depth - 1);
        }

        assertEquals((width - 1) * (depth - 1) + 1, DevstoneUtilsTestCase.countAtomics(HORoot));
        assertEquals(expectedIC, DevstoneUtilsTestCase.countIC(HORoot));
        assertEquals((width + 1) * (depth - 1) + 1, DevstoneUtilsTestCase.countEIC(HORoot));
        assertEquals(width * (depth - 1) + 1, DevstoneUtilsTestCase.countEOC(HORoot));
    }

    @Test
    @Override
    protected void testBehaviorSequential() throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        super.testBehaviorSequential();
    }

    @Override
    protected void testBehavior(Class<?> coordClass, int depth, int width, int int_cycles, int ext_cycles) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        depth = width = 3;
        DEVStone HOEnv = new DEVStone("root", "HO", depth, width, int_cycles, ext_cycles, true);
        Constructor<?> cons = coordClass.getConstructor(Coupled.class);
        Coordinator coordinator = (Coordinator) cons.newInstance(HOEnv);
        coordinator.initialize();
        coordinator.simulate(Double.MAX_VALUE);
        Coupled HORoot = (Coupled) HOEnv.getComponentByName("root_ho");
        int intTrans = DevstoneUtilsTestCase.countInternalTransitions(HORoot);
        int extTrans = DevstoneUtilsTestCase.countExternalTransitions(HORoot);
        int expectedTrans = (((width - 1) * width) / 2) * (depth - 1) + 1;
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
        this.checkInvalidInputs(HO.class);
    }


}