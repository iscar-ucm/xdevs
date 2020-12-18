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
public class Node1 {
    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        GptDistributed gpt = new GptDistributed(5, 100, File.separator + "tmp" + File.separator + "example.xml");
        //SimulatorDistributed p = new SimulatorDistributed(gpt, "172.17.0.3",5001,6001);
        System.out.println("Run Generator Atomic....");
        SimulatorDistributed p = new SimulatorDistributed(gpt, "127.0.0.1", 5001, 6001);  
    }
}
