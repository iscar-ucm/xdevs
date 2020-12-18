/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.util.logging.Level;
import xdevs.core.simulation.distributed.SimulatorDistributed;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author Almendras
 */
public class Node3 {
    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        GptDistributed gpt = new GptDistributed(5, 100, "gpt.xml");
        //SimulatorDistributed p = new SimulatorDistributed(gpt, "172.17.0.5",5003,6003);
        System.out.println("Run Tranducer Atomic....");
        SimulatorDistributed p = new SimulatorDistributed(gpt, "127.0.0.1",5003,6003);
    }
}
