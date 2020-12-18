/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.File;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.AbstractSimulator;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.util.Constants;

/**
 *
 * @author Almendras
 */
public class CoordinatorDistributed extends AbstractSimulator{
    // CoordinatorDistributed constants
    private static final Logger LOGGER = Logger.getLogger(CoordinatorDistributed.class.getName());
    
    // CoordinatorDistributed attributes
    private Node whoami;
    private ArrayList<Node> workers = new ArrayList<>();
    private ExecutorService executor;
    
    // CoordinatorDistributed Constructors
    public CoordinatorDistributed(SimulationClock clock, DistributedInterface mainModel) {
        super(clock);
        String simulationPlane = mainModel.getSimulationPlane();
        this.deploySimulationPlane(simulationPlane);
        this.executor = Executors.newFixedThreadPool(this.workers.size());  
        System.out.println("I am: " + this.whoami.getName());
        System.out.println("Workers: "+this.workers.toString());  
    }
    
    public CoordinatorDistributed(DistributedInterface mainModel){
        this(new SimulationClock(), mainModel);
    }    

    // CoordinatorDistributed methods    
    private void deploySimulationPlane(String simulationPlane){
        try{
            Document docXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(simulationPlane));
            Element xmlSimulation = (Element)docXML.getElementsByTagName("simulation").item(0);
            Element xmlNode = (Element) xmlSimulation.getElementsByTagName("coordinator").item(0);
            // Get coordinator data
            Node n = new Node(xmlNode.getAttribute("name"),
                        xmlNode.getAttribute("host"),
                        Integer.parseInt(xmlNode.getAttribute("mainPort")),
                        Integer.parseInt(xmlNode.getAttribute("auxPort"))
                     );
            this.whoami = n;
            // Get workers data
            NodeList xmlModels = docXML.getElementsByTagName("atomic");
            for(int i = 0; i < xmlModels.getLength(); ++i) {
                xmlNode = (Element)xmlModels.item(i);
                n = new Node(xmlNode.getAttribute("name"),
                        xmlNode.getAttribute("host"),
                        Integer.parseInt(xmlNode.getAttribute("mainPort")),
                        Integer.parseInt(xmlNode.getAttribute("auxPort"))
                    );
                workers.add(n);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }          
    }    

    
    public LinkedList<DistributedTask> executeTasksList(int command) {
        LinkedList<DistributedTask> distributedTasks = new LinkedList<>();
        if(command == Commands.BUILD_HIERARCHY){
            for (Node node : workers) {
                distributedTasks.add(new DistributedTask(node, command, node.name));
            }
        } else{
            for (Node node : workers) {
                distributedTasks.add(new DistributedTask(node, command, String.valueOf(clock.getTime())));
            }                        
        }        
        return distributedTasks;
    }    

    public void buildHierarchy() {
        try {            
            executor.invokeAll(executeTasksList(Commands.BUILD_HIERARCHY));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }            
    }
    
    @Override
    public void initialize() {
        this.buildHierarchy();
        try {            
            executor.invokeAll(executeTasksList(Commands.INITIALIZE));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }          
        tL = clock.getTime();
        tN = tL + ta();        
    }

    @Override
    public void exit() {
        MessageDistributed md;
        PingMessage pm;
        for (Node node : workers) {
            md = new MessageDistributed(Commands.EXIT, String.valueOf(clock.getTime()));
            pm = new PingMessage(md, node.host, node.port);
            md = pm.ping();
            md = new MessageDistributed(Commands.EXIT_AUX, String.valueOf(clock.getTime()));
            pm = new PingMessage(md, node.host, node.auxPort);
            md = pm.ping();            
        }        
    }
    
    @Override
    public double ta() {
        double tn = Constants.INFINITY;
        try {                        
            List<Future<String>> tas = executor.invokeAll(executeTasksList(Commands.TA));
            for (Future<String> ta : tas) {
                if (Double.valueOf(ta.get()) < tn) {
                    tn = Double.valueOf(ta.get()); // simulator.getTN();
                } 
            }            
        } catch(ExecutionException | InterruptedException e){
            LOGGER.severe(e.getLocalizedMessage());
        }       
        return tn - clock.getTime();
    }
    
    @Override
    public void lambda() {       
        try {            
            executor.invokeAll(executeTasksList(Commands.LAMBDA));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }       
        propagateOutput();        
    }
    
    public void propagateOutput() {
        try {            
            executor.invokeAll(executeTasksList(Commands.PROPAGATE_OUTPUT));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }           
    }
    
    @Override
    public void deltfcn(){
        try {            
            executor.invokeAll(executeTasksList(Commands.DELTFCN));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }          
        tL = clock.getTime();
        tN = tL + ta();
    }
    
    @Override
    public void clear() {
        try {            
            executor.invokeAll(executeTasksList(Commands.CLEAR));
        } catch (InterruptedException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }         
    }
    
    public void simulate(long numIterations) {
        LOGGER.fine("START SIMULATION");
        clock.setTime(tN);
        long counter;
        for (counter = 1; counter < numIterations && clock.getTime() < Constants.INFINITY; counter++) {
            lambda();
            deltfcn();
            clear();
            clock.setTime(tN);
        }
        executor.shutdown();
        LOGGER.fine("END SIMULATION");
    }

    public void simulate(double timeInterval) {
        LOGGER.fine("START SIMULATION");
        clock.setTime(tN);
        double tF = clock.getTime() + timeInterval;
        while (clock.getTime() < Constants.INFINITY && clock.getTime() < tF) {
            lambda();
            deltfcn();
            clear();
            clock.setTime(tN);
        }
        executor.shutdown();
        LOGGER.fine("END SIMULATION");     
    }
    
    @Override
    public Coupled getModel() {
        return null;
    }    
}
