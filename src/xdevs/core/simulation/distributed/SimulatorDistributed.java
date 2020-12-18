/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Port;
import xdevs.core.simulation.AbstractSimulator;
import xdevs.core.simulation.SimulationClock;

/**
 *
 * @author Almendras
 */
public class SimulatorDistributed extends AbstractSimulator{
    // SimulatorDistributed constants
    public static final String DEFAULT_NAME = SimulatorDistributed.class.getSimpleName();
    
    // SimulatorDistributed attributes
    protected Atomic model;    
    
    private DistributedInterface mainModel;    
    private Node whoami;
    private Node coordinator;
    private ArrayList<Node> allNodes = new ArrayList<>();
    private ArrayList<Connection> allConnections = new ArrayList<>();        
    private boolean getOut = false;
    
    // SimulatorDistributed Constructors
    public SimulatorDistributed(DistributedInterface mainModel, String name, String host, int port, int auxPort){
        super(new SimulationClock());
        this.mainModel = mainModel;
        deploySimulationPlane(mainModel.getSimulationPlane());
        this.whoami = searchNode(host, port, auxPort);
        System.out.println("I am: " + this.whoami.getName());
        System.out.println("Coordinator: "+this.coordinator.getName());
        System.out.println("Nodes: "+this.allNodes.toString());
        System.out.println("connections: "+this.allConnections.toString());        
        this.run();
    }
    
    public SimulatorDistributed(DistributedInterface mainModel, String host, int port, int auxPort){
        this(mainModel, DEFAULT_NAME, host, port, auxPort);
    }
    
    public boolean isGetOut() {    
        return getOut;
    }
    
    // CoordinatorDistributed methods
    private void deploySimulationPlane(String simulationPlane) {
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
            this.coordinator = n;            
            // Get workers data
            NodeList xmlModels = docXML.getElementsByTagName("atomic");
            for(int i = 0; i < xmlModels.getLength(); ++i) {
                xmlNode = (Element)xmlModels.item(i);
                n = new Node(xmlNode.getAttribute("name"),
                        xmlNode.getAttribute("host"),
                        Integer.parseInt(xmlNode.getAttribute("mainPort")),
                        Integer.parseInt(xmlNode.getAttribute("auxPort"))
                    );
                allNodes.add(n);
            }   
            // Get connections data
            NodeList xmlConnections = docXML.getElementsByTagName("connection");
            for(int i = 0; i < xmlConnections.getLength(); ++i) {
                xmlNode = (Element)xmlConnections.item(i);
                Connection conn = new Connection(xmlNode.getAttribute("portFrom"), searchNode(xmlNode.getAttribute("atomicFrom")), xmlNode.getAttribute("portTo"), searchNode(xmlNode.getAttribute("atomicTo")));
                allConnections.add(conn);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }         
       
    }    

    private Node searchNode(String nodeName){        
        for(Node n : allNodes) {
            if(n.name.equals(nodeName)) {
                return n;
            }
        }
        return null;
    }

    private Node searchNode(String host, int port, int auxPort){        
        for(Node n : allNodes) {
            if( n.host.equals(host) && (n.port==port) && (n.auxPort==auxPort)) {
                return n;
            }
        }
        return null;
    }

    @Override
    public void initialize() {
        model.initialize();
        tL = clock.getTime();
        tN = tL + model.ta();
    }
    
    @Override
    public void exit() {
        model.exit();
    }

    @Override
    public double ta() {
        return model.ta();
    }

    @Override
    public void lambda() {
        if (clock.getTime() == tN) {
            model.lambda();
        }
    }

    public void propagateOutput() {
        MessageDistributed md;        
        String nameModel = this.model.getName();
        for(Connection c : this.allConnections) {            
            if( c.getFromNode().getName().equals(nameModel) ) {
                md = new MessageDistributed(Commands.PROPAGATE_OUTPUT_N2N, 
                                            c.getTo(), 
                                            this.model.getOutPort(c.getFrom()).getValues()
                                           );
                PingMessage pm = new PingMessage(md, c.getToNode().getHost(), c.getToNode().getAuxPort());
                md = pm.ping();
            }
        }
    }
    
    public void propagateOutputN2N( Collection valuesPort, String portName ) {
        this.model.getInPort(portName).addValues(valuesPort);        
    }    
    
    @Override
    public void deltfcn() {
        double t = clock.getTime();
        boolean isInputEmpty = model.isInputEmpty();
        if (isInputEmpty && t != tN) {
            return;
        } else if (!isInputEmpty && t == tN) {
            model.deltcon();
        } else if (isInputEmpty && t == tN) {
            model.deltint();
        } else if (!isInputEmpty && t != tN) {
            double e = t - tL;
            model.setSigma(model.getSigma() - e);
            model.deltext(e);
        }
        tL = t;
        tN = tL + model.ta();
    }

    @Override
    public void clear() {
        Collection<Port<?>> inPorts;
        inPorts = model.getInPorts();
        for (Port<?> port : inPorts) {
            port.clear();
        }
        Collection<Port<?>> outPorts;
        outPorts = model.getOutPorts();
        for (Port<?> port : outPorts) {
            port.clear();
        }
    }
    
    @Override
    public Atomic getModel() {
        return model;
    }  
    
    public MessageDistributed interpreter( MessageDistributed md ){
        MessageDistributed response = null;
        Date date = new Date();
        DateFormat now = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.");
        try {            
            switch(md.getCommand()){
                case Commands.NONE:
                    response = new MessageDistributed("NONE");
                    break;                  
                case Commands.BUILD_HIERARCHY:                    
                    model = mainModel.returnModel(md.getMessage());
                    whoami.setName(md.getMessage());                    
                    response = new MessageDistributed("BUILD_HIERARCHY: I'AM "+md.getMessage()+" At " + now.format(date));
                    break;        
                case Commands.INITIALIZE:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.initialize();
                    response = new MessageDistributed("INITIALIZE: OK At " + now.format(date));
                    break;
                case Commands.TA:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    response = new MessageDistributed(String.valueOf(this.getTN()));
                    break;
                case Commands.LAMBDA:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.lambda();
                    response = new MessageDistributed("LAMBDA: OK At " + now.format(date));
                    break;
                case Commands.PROPAGATE_OUTPUT:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.propagateOutput();
                    response = new MessageDistributed("PROPAGATE_OUTPUT: OK At " + now.format(date));
                    break;
                case Commands.PROPAGATE_OUTPUT_N2N:                                        
                    this.propagateOutputN2N(md.getValuesPort(),md.getMessage());
                    response = new MessageDistributed("PROPAGATE_OUTPUT_N2N: OK At " + now.format(date));
                    break;                    
                case Commands.DELTFCN:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.deltfcn();
                    response = new MessageDistributed("DELTFCN: OK At " + now.format(date));
                    break;
                case Commands.CLEAR:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.clear();
                    response = new MessageDistributed("CLEAR: OK At " + now.format(date));
                    break;
                case Commands.EXIT:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.exit();
                    response = new MessageDistributed("EXIT: OK At " + now.format(date));
                    this.getOut = true;
                    break;
                case Commands.EXIT_AUX:                    
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    response = new MessageDistributed("EXIT_AUX: OK At " + now.format(date));
                    break;                      
                default:
                    response = new MessageDistributed("BAD_COMMAND");
                    break;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public void run() {			
            // For to attend the communication with the coordinator
            Thread mainDaemon = new Thread(new DistributedDaemon(this.whoami.getPort(),this));
            mainDaemon.start();
            // For to attend the communication with the workers (At this case to propagate)
            Thread auxDaemon = new Thread(new DistributedDaemon(this.whoami.getAuxPort(),this));
            auxDaemon.start();
    }

}
