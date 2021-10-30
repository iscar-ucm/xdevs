/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.examples.distributed.gpt;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import xdevs.core.simulation.distributed.CoordinatorDistributed;
import xdevs.core.simulation.distributed.SimulatorDistributed;
import xdevs.core.modeling.distributed.CoupledDistributed;
import xdevs.core.util.DevsLogger;
/**
 *
 * @author Almendras
 */
public class Node {
    
    private static final Logger LOGGER = Logger.getLogger(Node.class.getName());
    
    public static void main(String[] args) {
        if(args.length>0) {
            String fileName = args[0];
            Element xmlCoupled;
            try {
                Document xmlCoupledModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(fileName));
                xmlCoupled = (Element) xmlCoupledModel.getElementsByTagName("coupled").item(0);
            } catch (IOException | ParserConfigurationException | SAXException ex) {
                Logger.getLogger(CoupledDistributed.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            DevsLogger.setup(Level.INFO);
            if(args.length==2){ // Simulator
                String atomicName = args[1];                                        
                CoupledDistributed gpt = new CoupledDistributed(xmlCoupled);
                LOGGER.info("Run "+atomicName+" .....");
                new SimulatorDistributed(gpt, atomicName);
            }else if(args.length==1){ // Coordinator
                LOGGER.info("Run Coordinator .....");
                CoupledDistributed gpt = new CoupledDistributed(xmlCoupled);
                CoordinatorDistributed coordinator = new CoordinatorDistributed(gpt);
                long start = System.currentTimeMillis();
                coordinator.initialize();
                coordinator.simulate(Long.MAX_VALUE);
                coordinator.exit();
                long end = System.currentTimeMillis();
                double time = (end - start) / 1000.0;
                LOGGER.info("TIME: " + time);
            }
        }else {
            LOGGER.info("Check the arguments: Two for Simulator(file name and atomic name) and One for Coordinator(file name)");
        }
    }
}
