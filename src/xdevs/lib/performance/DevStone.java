/*
 * Copyright (C) 2014-2016 José Luis Risco Martín <jlrisco@ucm.es>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.lib.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.parallel.CoordinatorParallel;
import xdevs.core.util.DevsLogger;

/**
 * Coupled model to study the performance using DEVStone
 *
 * @author José Luis Risco Martín
 */
public abstract class DevStone extends Coupled {

    private static final Logger LOGGER = Logger.getLogger(DevStone.class.getName());

    public static enum BenchmarkType {
        LI, HI, HO, HOmem, HOmod
    };

    public Port<Integer> iIn = new Port<>("in");
    public Port<Integer> oOut = new Port<>("out");
    private int width;
    private int depth;

    public DevStone(String name, int width, int depth) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
        this.width = width;
        this.depth = depth;
    }

    public abstract int getNumOfAtomic(int width, int depth);

    public abstract int getNumDeltExts(int maxEvents, int width, int depth);

    public abstract int getNumDeltInts(int maxEvents, int width, int depth);

    public abstract long getNumOfEvents(int maxEvents, int width, int depth);

    public static void printUsage() {
        System.err.println(
                "Usage: java DEVStone.java <MODEL> <WIDTH> <DEPTH> <DELAY> <COORDINATOR> <FLATTEN> <LOGGER_PATH>");
        System.err.println("    - <MODEL>: DEVStone model (LI, HI, HO, or HOmod)");
        System.err.println("    - <WIDTH>: DEVStone model's width (it must be an integer)");
        System.err.println("    - <DEPTH>: DEVStone model's depth (it must be an integer)");
        System.err.println("    - <DELAY>: DEVStone model's internal and external transition delay in seconds");
        System.err.println(
                "    - <COORDINATOR>: Desired xDEVS coordinator type for simulating the model (Null -just saves the XML model-, Coordinator, CoordinatorParallel)");
        System.err.println("    - <FLATTEN>: Flag for flattening the model before simulation (true or false)");
    }

    public static void main(String[] args) {
        if (args.length != 7) {
            System.err.println("Invalid number of arguments.");
            printUsage();
            System.err.println("Using defaults: HO 300 300 0 Coordinator false logger.log");
            args = new String[] { "HO", "300", "300", "0", "Coordinator", "false", "logger.log" };
        }
        DevsLogger.setup(args[6], Level.INFO);
        BenchmarkType benchmarkType;
        int width;
        int depth;
        double delayTime;
        try {
            benchmarkType = BenchmarkType.valueOf(args[0]);
            depth = Integer.parseInt(args[1]);
            width = Integer.parseInt(args[2]);
            delayTime = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Some parameters format are incorrect");
            printUsage();
            throw new RuntimeException();
        }
        String coordinatorType = args[4];
        boolean flatten = Boolean.parseBoolean(args[5]);

        // Atomic number of internal and external transitions, as well as number of
        // events
        DevStoneAtomic.NUM_DELT_INTS = 0;
        DevStoneAtomic.NUM_DELT_EXTS = 0;
        DevStoneAtomic.NUM_OF_EVENTS = 0;
        // Generator parameters:
        double preparationTime = 0.0;
        double period = 1.0;
        int maxEvents = 1;

        long modelStart = System.currentTimeMillis();

        Coupled framework = new Coupled("DevStone" + benchmarkType.toString());
        DevStoneGenerator generator = new DevStoneGenerator("Generator", preparationTime, period, maxEvents);
        framework.addComponent(generator);
        DevStone stoneCoupled;
        switch (benchmarkType) {
        case LI:
            stoneCoupled = new DevStoneCoupledLI("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        case HI:
            stoneCoupled = new DevStoneCoupledHI("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        case HO:
            stoneCoupled = new DevStoneCoupledHO("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        case HOmem:
            stoneCoupled = new DevStoneCoupledHOmem("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        case HOmod:
            stoneCoupled = new DevStoneCoupledHOmod("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        default:
            stoneCoupled = new DevStoneCoupledLI("C", width, depth, preparationTime, delayTime, delayTime);
            break;
        }
        framework.addComponent(stoneCoupled);
        framework.addCoupling(generator.oOut, stoneCoupled.iIn);
        switch (benchmarkType) {
        case HO:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHO) stoneCoupled).iInAux);
            break;
        case HOmem:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmem) stoneCoupled).iInAux);
            break;
        case HOmod:
            framework.addCoupling(generator.oOut, ((DevStoneCoupledHOmod) stoneCoupled).iInAux);
            break;
        default:
            break;
        }

        long modelStop = System.currentTimeMillis();
        double modelCreationTime = ((modelStop - modelStart) / 1e3);

        long coordStart = System.currentTimeMillis();
        Coordinator coordinator;
        switch (coordinatorType) {
        case "Null":
            try {
                if (flatten) {
                    framework.flatten();
                }
                var writer = new BufferedWriter(new FileWriter(new File(benchmarkType.toString() + "_W-" + width + "_D-" + depth + "_T-" + delayTime + ".xml")));
                writer.write(framework.toXml());
                writer.close();
            } catch (IOException e) {
                System.err.println(e.getLocalizedMessage());
            }
            return;
        case "Coordinator":
            coordinator = new Coordinator(framework, flatten);
            break;
        case "CoordinatorParallel":
            coordinator = new CoordinatorParallel(framework);
            break;
        default:
            System.err.println("xDEVS coordinator type not found");
            printUsage();
            throw new RuntimeException();
        }
        coordinator.initialize();
        long coordStop = System.currentTimeMillis();
        double engineSetupTime = ((coordStop - coordStart) / 1e3);

        // Theoretical values
        int numDeltInts = stoneCoupled.getNumDeltInts(maxEvents, width, depth);
        int numDeltExts = stoneCoupled.getNumDeltExts(maxEvents, width, depth);
        long numOfEvents = stoneCoupled.getNumOfEvents(maxEvents, width, depth);

        long simulationStart = System.currentTimeMillis();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        long simulationStop = System.currentTimeMillis();
        double simulationTime = (simulationStop - simulationStart) / 1e3;

        LOGGER.info(
                "MODEL,MAXEVENTS,WIDTH,DEPTH,NUM_DELT_INTS,NUM_DELT_EXTS,NUM_OF_EVENTS,SIMULATION_TIME,MODEL_CREATION_TIME,ENGINE_SETUP_TIME");
        String stats;
        if (DevStoneAtomic.NUM_DELT_INTS != numDeltInts || DevStoneAtomic.NUM_DELT_EXTS != numDeltExts
                || DevStoneAtomic.NUM_OF_EVENTS != numOfEvents) {
            DevStoneAtomic.NUM_DELT_INTS = -DevStoneAtomic.NUM_DELT_INTS;
            DevStoneAtomic.NUM_DELT_EXTS = -DevStoneAtomic.NUM_DELT_EXTS;
            DevStoneAtomic.NUM_OF_EVENTS = -DevStoneAtomic.NUM_OF_EVENTS;
        }
        stats = args[0] + "," + maxEvents + "," + width + "," + depth + "," + DevStoneAtomic.NUM_DELT_INTS + ","
                + DevStoneAtomic.NUM_DELT_EXTS + "," + DevStoneAtomic.NUM_OF_EVENTS + "," + simulationTime + ","
                + modelCreationTime + "," + engineSetupTime;
        LOGGER.info(stats);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
