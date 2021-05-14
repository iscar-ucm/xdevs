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

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;
import java.util.logging.Logger;
import xdevs.core.modeling.Component;
import xdevs.core.modeling.Coupling;
import xdevs.core.simulation.parallel.CoordinatorParallel;

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

    public DevStone(String name) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
    }

    public abstract int getNumOfAtomic(int width, int depth);

    public abstract int getNumDeltExts(int maxEvents, int width, int depth);

    public abstract int getNumDeltInts(int maxEvents, int width, int depth);

    public abstract long getNumOfEvents(int maxEvents, int width, int depth);

    public static void printUsage() {
        System.err.println("Usage: java DEVStone.java <MODEL> <WIDTH> <DEPTH> <INTERNAL DELAY> <EXTERNAL DELAY> <COORDINATOR> <FLATTEN>");
        System.err.println("    - <MODEL>: DEVStone model (LI, HI, HO, or HOmod)");
        System.err.println("    - <WIDTH>: DEVStone model's width (it must be an integer)");
        System.err.println("    - <DEPTH>: DEVStone model's depth (it must be an integer)");
        System.err.println("    - <INTERNAL_DELAY>: DEVStone model's internal transition delay in seconds");
        System.err.println("    - <EXTERNAL_DELAY>: DEVStone model's external transition delay in seconds");
        System.err.println("    - <COORDINATOR>: Desired xDEVS coordinator type for simulating the model (Coordinator, CoordinatorParallel)");
        System.err.println("    - <FLATTEN>: Flag for flattening the model before simulation (true or false)");
    }

    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        if (args.length != 7) {
            System.err.println("Invalid number of arguments.");
            printUsage();
            System.err.println("Using defaults: HO 300 300 0 0 Coordinator false");
            args = new String[]{"HO","300","300","0","0","Coordinator", "false"};
        }
        BenchmarkType benchmarkType;
        int width;
        int depth;
        double intDelayTime;
        double extDelayTime;
        try {
            benchmarkType = BenchmarkType.valueOf(args[0]);
            depth = Integer.parseInt(args[1]);
            width = Integer.parseInt(args[2]);
            intDelayTime = Double.parseDouble(args[3]);
            extDelayTime = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            LOGGER.severe("Some parameters format are incorrect");
            printUsage();
            throw new RuntimeException();
        }
        String coordinatorType = args[5];
        boolean flatten = Boolean.parseBoolean(args[6]);

        // Atomic number of internal and external transitions, as well as number of events
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
                stoneCoupled = new DevStoneCoupledLI("C", width, depth, preparationTime, intDelayTime, extDelayTime);
                break;
            case HI:
                stoneCoupled = new DevStoneCoupledHI("C", width, depth, preparationTime, intDelayTime, extDelayTime);
                break;
            case HO:
                stoneCoupled = new DevStoneCoupledHO("C", width, depth, preparationTime, intDelayTime, extDelayTime);
                break;
            case HOmem:
                stoneCoupled = new DevStoneCoupledHOmem("C", width, depth, preparationTime, intDelayTime, extDelayTime);
                break;
            case HOmod:
                stoneCoupled = new DevStoneCoupledHOmod("C", width, depth, preparationTime, intDelayTime, extDelayTime);
                break;
            default:
                stoneCoupled = new DevStoneCoupledLI("C", width, depth, preparationTime, intDelayTime, extDelayTime);
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
        LOGGER.info("Model creation time: " + ((modelStop - modelStart) / 1e3));

        long coordStart = System.currentTimeMillis();
        Coordinator coordinator;
        switch (coordinatorType) {
            case "Coordinator":
                coordinator = new Coordinator(framework, flatten);
                break;
            case "CoordinatorParallel":
                coordinator = new CoordinatorParallel(framework);
                break;
            default:
                LOGGER.info("xDEVS coordinator type not found");
                printUsage();
                throw new RuntimeException();
        }
        coordinator.initialize();
        long coordStop = System.currentTimeMillis();
        LOGGER.info("Engine setup time: " + ((coordStop - coordStart) / 1e3));       
        
        // Theoretical values
        int numDeltInts = stoneCoupled.getNumDeltInts(maxEvents, width, depth);
        int numDeltExts = stoneCoupled.getNumDeltExts(maxEvents, width, depth);
        long numOfEvents = stoneCoupled.getNumOfEvents(maxEvents, width, depth);

        long simulationStart = System.currentTimeMillis();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        long simulationStop = System.currentTimeMillis();
        double simulationTime = (simulationStop - simulationStart) / 1e3;
        LOGGER.info("Simulation time: " + simulationTime);

        LOGGER.info("MAXEVENTS;WIDTH;DEPTH;NUM_DELT_INTS;NUM_DELT_EXTS;NUM_OF_EVENTS;TIME");
        String stats;
        if (DevStoneAtomic.NUM_DELT_INTS == numDeltInts && DevStoneAtomic.NUM_DELT_EXTS == numDeltExts && DevStoneAtomic.NUM_OF_EVENTS == numOfEvents) {
            stats = maxEvents + ";" + width + ";" + depth + ";" + DevStoneAtomic.NUM_DELT_INTS + ";" + DevStoneAtomic.NUM_DELT_EXTS + ";" + DevStoneAtomic.NUM_OF_EVENTS + ";" + simulationTime;
        } else {
            stats = "ERROR: NumDeltInts or NumDeltExts or NumOfEvents do not match the theoretical values (between brackets): " + DevStoneAtomic.NUM_DELT_INTS + ";[" + numDeltInts + "];" + DevStoneAtomic.NUM_DELT_EXTS + ";[" + numDeltExts + "];" + DevStoneAtomic.NUM_OF_EVENTS + ";[" + numOfEvents + "];" + simulationTime;
        }
        LOGGER.info(stats);
    }

    public String toXML(int level) {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < level; ++i) {
            tabs.append("\t");
        }
        StringBuilder builder = new StringBuilder();
        if (level == 0) {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        }
        builder.append(tabs).append("<coupled name=\"").append(super.getName()).append("\">\n");
        // Components
        Collection<Component> components = super.getComponents();
        components.forEach((component) -> {
            if (component instanceof Coupled) {
                int levelAux = level + 1;
                builder.append(((DevStone) component).toXML(levelAux));
            } else {
                builder.append(tabs).append("\t<atomic name=\"").append(component.getName()).append("\"/>\n");
            }
        });
        // Couplings
        LinkedList<Coupling<?>> couplings = super.getEIC();
        couplings.forEach((coupling) -> {
            builder.append(tabs).append("\t<connection ");
            builder.append("componentFrom=\"").append(coupling.getPortFrom().getParent().getName()).append("\" portFrom=\"").append(coupling.getPortFrom().getName());
            builder.append("\" componentTo=\"").append(coupling.getPortTo().getParent().getName()).append("\" portTo=\"").append(coupling.getPortTo().getName()).append("\"/>\n");
        });
        couplings = super.getIC();
        couplings.forEach((coupling) -> {
            builder.append(tabs).append("\t<connection ");
            builder.append("componentFrom=\"").append(coupling.getPortFrom().getParent().getName()).append("\" portFrom=\"").append(coupling.getPortFrom().getName());
            builder.append("\" componentTo=\"").append(coupling.getPortTo().getParent().getName()).append("\" portTo=\"").append(coupling.getPortTo().getName()).append("\"/>\n");
        });
        couplings = super.getEOC();
        couplings.forEach((coupling) -> {
            builder.append(tabs).append("\t<connection ");
            builder.append("componentFrom=\"").append(coupling.getPortFrom().getParent().getName()).append("\" portFrom=\"").append(coupling.getPortFrom().getName());
            builder.append("\" componentTo=\"").append(coupling.getPortTo().getParent().getName()).append("\" portTo=\"").append(coupling.getPortTo().getName()).append("\"/>\n");
        });
        builder.append(tabs).append("</coupled>\n");
        return builder.toString();
    }
}
