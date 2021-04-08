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
import xdevs.core.modeling.distributed.CoupledDistributed;
import xdevs.core.simulation.distributed.SimulatorDistributed;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author Almendras
 */
public class NodeTransducer {
    private static final Logger LOGGER = Logger.getLogger(NodeTransducer.class.getName());
    public static void main(String[] args) {
        if(args.length==0) {
            args = new String[]{"tmp" + File.separator + "gpt.xml"};
        }
        Element xmlCoupled;
        try {
            Document xmlCoupledModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0]));
            xmlCoupled = (Element) xmlCoupledModel.getElementsByTagName("coupled").item(0);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(CoupledDistributed.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        DevsLogger.setup(Level.INFO);
        CoupledDistributed gpt = new CoupledDistributed(xmlCoupled);
        LOGGER.info("Run Transducer Atomic....");
        new SimulatorDistributed(gpt, "transducer");
    }
}
