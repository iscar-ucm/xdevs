package xdevs.core.devstone;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.chained.CoordinatorChained;
import xdevs.core.simulation.chained.CoordinatorChainedParallel;
import xdevs.core.simulation.parallel.CoordinatorParallel;


public class DEVStone {

    public static void printUsage() {
        System.out.println("Usage: java DEVStone.java <MODEL> <DEPTH> <WIDTH> <INTERNAL DELAY> <EXTERNAL DELAY> <COORDINATOR> <FLATTEN>");
        System.out.println("    - <MODEL>: DEVStone model (LI, HI, HO, or HOmod)");
        System.out.println("    - <DEPTH>: DEVStone model's depth (it must be an integer)");
        System.out.println("    - <WIDTH>: DEVStone model's width (it must be an integer)");
        System.out.println("    - <INTERNAL_DELAY>: DEVStone model's internal transition delay in ms (it must be an integer)");
        System.out.println("    - <EXTERNAL_DELAY>: DEVStone model's external transition delay in ms (it must be an integer)");
        System.out.println("    - <COORDINATOR>: Desired xDEVS coordinator type for simulating the model (coord, chained, parallel, or chainedparallel)");
        System.out.println("    - <FLATTEN>: Flag for flattening the model before simulation (true or false)");
    }

    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Invalid number of arguments");
            printUsage();
            throw new RuntimeException();
        }
        String modelType = args[0];
        int depth;
        int width;
        int intDelay;
        int extDelay;
        try {
            depth = Integer.parseInt(args[1]);
            width = Integer.parseInt(args[2]);
            intDelay = Integer.parseInt(args[3]);
            extDelay = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Some parameters format are incorrect");
            printUsage();
            throw new RuntimeException();
        }
        String coordType = args[5];
        boolean flatten = Boolean.parseBoolean(args[6]);

        // TODO start counting time now
        Coupled model;
        switch (modelType) {
            case "LI":
                model = new LI("li", depth, width, intDelay, extDelay);
                break;
            case "HI":
                model = new HI("hi", depth, width, intDelay, extDelay);
                break;
            case "HO":
                model = new HO("ho", depth, width, intDelay, extDelay);
                break;
            case "HOmod":
                model = new HOmod("homod", depth, width, intDelay, extDelay);
                break;
            default:
                System.out.println("DEVStone model not found");
                printUsage();
                throw new RuntimeException();
        }
        // TODO stop counting time now

        // TODO start counting time now
        Coordinator coord;
        switch (coordType) {
            case "coord":
                coord = new Coordinator(model, flatten);
                break;
            case "chained":
                coord = new CoordinatorChained(model, flatten);
                break;
            case "parallel":
                coord = new CoordinatorParallel(model);
                break;
            case "chainedparallel":
                coord = new CoordinatorChainedParallel(model);
                break;
            default:
                System.out.println("xDEVS coordinator type not found");
                printUsage();
                throw new RuntimeException();
        }
        coord.initialize();
        // TODO stop counting time now

        // TODO start counting time now
        //coord.simInject(model.iIn, 0);
        coord.simulate(Long.MAX_VALUE);
        // TODO stop counting time now
        coord.exit();
    }
}