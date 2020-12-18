/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import xdevs.core.examples.efp.Generator;
import xdevs.core.examples.efp.Processor;
import xdevs.core.examples.efp.Transducer;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.distributed.CoordinatorDistributed;
import xdevs.core.util.DevsLogger;
import xdevs.core.simulation.distributed.DistributedInterface;

/**
 *
 * @author Almendras
 */
public class GptDistributed extends Coupled implements DistributedInterface{
           
    // Atributtes GptDistributed
    private String simulationPlane;
    private double period;
    private double processingTime;
    private double observationTime;
            
    // Constructors GptDistributed
    public GptDistributed(String name, double period, double observationTime, String simulationPlane) {
    	this(period, observationTime, simulationPlane);           
        super.name= name;
    }
    public GptDistributed(double period, double observationTime, String simulationPlane) {
    	super(GptDistributed.class.getSimpleName());        
        this.period = period;
        this.processingTime = period*3;
        this.observationTime = observationTime;
        this.simulationPlane = simulationPlane;
    }    
    
    // Methods GptDistributed 
    @Override
    public Atomic returnModel(String ClassName) {
        if (ClassName.compareTo("Generator") == 0) {
            return new Generator(ClassName, this.period);
        } else if (ClassName.compareTo("Processor") == 0) {
            return new Processor(ClassName, this.processingTime);
        } else {
            return new Transducer(ClassName, this.observationTime);
        }
    }

    @Override
    public String getSimulationPlane() {
        return simulationPlane;
    }
    private static final Logger LOGGER = Logger.getLogger(GptDistributed.class.getName());
    public static void main(String[] args) {
        DevsLogger.setup(Level.INFO);
        System.out.println("Run Coordinator Atomic....");
        GptDistributed gpt = new GptDistributed("GPT", 5, 100, File.separator + "tmp" + File.separator + "example.xml"); 
        CoordinatorDistributed coordinator = new CoordinatorDistributed(gpt);
        long start = System.currentTimeMillis();
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        LOGGER.info("TIME: " + time); 
    }    
}
