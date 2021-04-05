/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

/**
 *
 * @author Almendras
 */
public class Commands {
    // Commands constants
    public static final int NONE = 0; //This does not do anything.   
    public static final int INITIALIZE = 2; //It allows to initialize the simulator.
    public static final int TA = 3; //It allows find the advance time.
    public static final int LAMBDA = 4; //It allows to execute the lambda method.
    public static final int PROPAGATE_OUTPUT = 5; //It allows propagate the outputs for each simulator.    
    public static final int PROPAGATE_OUTPUT_N2N = 6; //It allows propagate the outputs of node to node.    
    public static final int DELTFCN = 7; //It allows to execute the delta methods.    
    public static final int CLEAR = 8; //It allows to refresh ports.    
    public static final int EXIT = 15; //It allows to exit of each simulator.    
    public static final int EXIT_AUX = 16; //It allows to off the auxiliary port. 
}
