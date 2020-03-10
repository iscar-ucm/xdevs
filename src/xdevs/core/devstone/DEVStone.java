package xdevs.core.devstone;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.simulation.chained.CoordinatorChained;
import xdevs.core.simulation.chained.CoordinatorChainedParallel;
import xdevs.core.simulation.parallel.CoordinatorParallel;


public class DEVStone extends Coupled {

    public DEVStone(String name, String modelType, int depth, int width, int intDelay, int extDelay) {
        super(name);

        Coupled model;
        switch (modelType) {
            case "LI":
                model = new LI(name + "_li", depth, width, intDelay, extDelay);
                break;
            case "HI":
                model = new HI(name + "_hi", depth, width, intDelay, extDelay);
                break;
            case "HO":
                model = new HO(name + "_ho", depth, width, intDelay, extDelay);
                break;
            case "HOmod":
                model = new HOmod(name + "_homod", depth, width, intDelay, extDelay);
                break;
            default:
                System.out.println("DEVStone model not found");
                printUsage();
                throw new RuntimeException();
        }
        this.addComponent(model);

        Seeder seeder = new Seeder(name + "_seeder", 30);
        this.addComponent(seeder);

        if (model instanceof DEVStoneWrapper) {
            this.addCoupling(seeder.oOut,((DEVStoneWrapper)model).iIn);
            if (model instanceof HO) {
                this.addCoupling(seeder.oOut,((HO)model).iIn2);
            }
        } else {
            this.addCoupling(seeder.oOut,((HOmod)model).iIn);
            this.addCoupling(seeder.oOut,((HOmod)model).iIn2);
        }

    }

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

        long modelStart = System.nanoTime();

        DEVStone devstone = new DEVStone("devstone", modelType, depth, width, intDelay, extDelay);
        long modelStop = System.nanoTime();
        System.out.println("Model created. Elapsed time: " + (modelStop - modelStart));

        long coordStart = System.nanoTime();
        Coordinator coord;
        switch (coordType) {
            case "coord":
                coord = new Coordinator(devstone, flatten);
                break;
            case "chained":
                coord = new CoordinatorChained(devstone, flatten);
                break;
            case "parallel":
                coord = new CoordinatorParallel(devstone);
                break;
            case "chainedparallel":
                coord = new CoordinatorChainedParallel(devstone);
                break;
            default:
                System.out.println("xDEVS coordinator type not found");
                printUsage();
                throw new RuntimeException();
        }
        coord.initialize();
        long coordStop = System.nanoTime();
        System.out.println("Coordinator created. Elapsed time: " + (coordStop - coordStart));

        long simulationStart = System.nanoTime();
        //coord.simInject(model.iIn, 0);
        coord.simulate(Long.MAX_VALUE);
        long simulationStop = System.nanoTime();
        System.out.println("Simulation finished. Elapsed time: " + (simulationStop - simulationStart));

        coord.exit();
    }
}
