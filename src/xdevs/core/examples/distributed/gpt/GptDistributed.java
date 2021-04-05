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
import xdevs.core.simulation.distributed.CoordinatorDistributed;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author Almendras
 */
public class GptDistributed extends CoupledDistributed {

    private static final Logger LOGGER = Logger.getLogger(GptDistributed.class.getName());

    public GptDistributed(Element xmlCoupled) {
        super(xmlCoupled);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"tmp" + File.separator + "gpt.xml"};
        }
        Element xmlCoupled;
        try {
            Document xmlCoupledModel = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(args[0]));
            xmlCoupled = (Element) xmlCoupledModel.getElementsByTagName("coupled").item(0);
            LOGGER.info(xmlCoupled.toString());
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return;
        }
        DevsLogger.setup(Level.INFO);
        LOGGER.info("Run Coordinator Atomic....");
        GptDistributed gpt = new GptDistributed(xmlCoupled);
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
