/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.io.File;
import java.util.logging.Level;
import xdevs.core.simulation.distributed.SimulatorDistributed;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author Almendras
 */
public class NodeProcessor {
    public static void main(String[] args) {       
        if(args.length==0) {
            args = new String[]{"5.0", "100.0", "tmp" + File.separator + "gpt.xml"};
        }
        DevsLogger.setup(Level.INFO);
        GptDistributed gpt = new GptDistributed(Double.valueOf(args[0]), Double.valueOf(args[1]), args[2]);
        //SimulatorDistributed p = new SimulatorDistributed(gpt, "172.17.0.4",5002,6002);
        System.out.println("Run Processor Atomic....");
        SimulatorDistributed p = new SimulatorDistributed(gpt, "127.0.0.1",5002,6002);
    }
}
