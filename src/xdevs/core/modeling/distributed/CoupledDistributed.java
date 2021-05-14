/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.modeling.distributed;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.distributed.CoordinatorDistributed;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author Almendras
 */
public class CoupledDistributed extends Coupled {

    private static final Logger LOGGER = Logger.getLogger(CoupledDistributed.class.getName());

    protected Element xmlCoupled;
    protected HashMap<String, String> hosts = new HashMap<>();
    protected HashMap<String, Integer> mainPorts = new HashMap<>();
    protected HashMap<String, Integer> auxPorts = new HashMap<>();

    public CoupledDistributed(Element xmlCoupled) {
        super(xmlCoupled.getAttribute("name"));
        hosts.put(xmlCoupled.getAttribute("name"), xmlCoupled.getAttribute("host"));
        mainPorts.put(xmlCoupled.getAttribute("name"), Integer.parseInt(xmlCoupled.getAttribute("mainPort")));
        auxPorts.put(xmlCoupled.getAttribute("name"), Integer.parseInt(xmlCoupled.getAttribute("auxPort")));
        // Creamos los distintos elementos
        NodeList xmlChildList = xmlCoupled.getChildNodes();
        for (int i = 0; i < xmlChildList.getLength(); ++i) {
            Node xmlNode = xmlChildList.item(i);
            String nodeName = xmlNode.getNodeName();
            switch (nodeName) {
                case "atomic":
                    try {
                    Element xmlChild = (Element) xmlNode;
                    Class<?> atomicClass = Class.forName(xmlChild.getAttribute("class"));
                    Constructor<?> constructor = atomicClass.getConstructor(new Class[]{Class.forName("org.w3c.dom.Element")});
                    Object atomicObject = constructor.newInstance(new Object[]{xmlChild});
                    this.addComponent((Atomic) atomicObject);
                    hosts.put(xmlChild.getAttribute("name"), xmlChild.getAttribute("host"));
                    mainPorts.put(xmlChild.getAttribute("name"), Integer.parseInt(xmlChild.getAttribute("mainPort")));
                    auxPorts.put(xmlChild.getAttribute("name"), Integer.parseInt(xmlChild.getAttribute("auxPort")));
                } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                    LOGGER.severe(ex.getLocalizedMessage());
                }
                break;
                case "connection":
                    Element xmlChild = (Element) xmlNode;
                    String componentFrom = xmlChild.getAttribute("componentFrom");
                    String portFrom = xmlChild.getAttribute("portFrom");
                    String componentTo = xmlChild.getAttribute("componentTo");
                    String portTo = xmlChild.getAttribute("portTo");
                    this.addCoupling(componentFrom, portFrom, componentTo, portTo);
                    break;
                default:
                    break;
            }
        }

    }

    public String getHost(String componentName) {
        return hosts.get(componentName);
    }

    public Integer getMainPort(String componentName) {
        return mainPorts.get(componentName);
    }

    public Integer getAuxPort(String componentName) {
        return auxPorts.get(componentName);
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
}
