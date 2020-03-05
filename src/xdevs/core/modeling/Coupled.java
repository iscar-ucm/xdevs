/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.core.modeling;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coupled extends Component {

    private static final Logger LOGGER = Logger.getLogger(Coupled.class.getName());

    // Coupled attributes
    protected LinkedList<Component> components = new LinkedList<>();
    protected LinkedList<Coupling<?>> ic = new LinkedList<>();
    protected LinkedList<Coupling<?>> eic = new LinkedList<>();
    protected LinkedList<Coupling<?>> eoc = new LinkedList<>();

    public Coupled(String name) {
        super(name);
    }

    public Coupled() {
        this(Coupled.class.getSimpleName());
    }

    public Coupled(Element xmlCoupled) {
        super(xmlCoupled.getAttribute("name"));
        // Creamos los distintos elementos
        NodeList xmlChildList = xmlCoupled.getChildNodes();
        for (int i = 0; i < xmlChildList.getLength(); ++i) {
            Node xmlNode = xmlChildList.item(i);
            Element xmlChild;
            String nodeName = xmlNode.getNodeName();
            switch (nodeName) {
                case "inport":
                    xmlChild = (Element) xmlNode;
                    super.addInPort(new Port(xmlChild.getAttribute("name")));
                    break;
                case "outport":
                    xmlChild = (Element) xmlNode;
                    super.addOutPort(new Port(xmlChild.getAttribute("name")));
                    break;
                case "coupled":
                    xmlChild = (Element) xmlNode;
                    this.addComponent(new Coupled(xmlChild));
                    break;
                case "atomic":
                    xmlChild = (Element) xmlNode;
                    try {
                        Class atomicClass = Class.forName(xmlChild.getAttribute("class"));
                        Constructor constructor = atomicClass.getConstructor(new Class[]{Class.forName("org.w3c.dom.Element")});
                        Object atomicObject = constructor.newInstance(new Object[]{xmlChild});
                        this.addComponent((Atomic) atomicObject);
                    } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                        LOGGER.severe(ex.getLocalizedMessage());
                    }
                    break;
                case "connection":
                    xmlChild = (Element) xmlNode;
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

    @Override
    public void initialize() {
    }

    @Override
    public void exit() {
    }

    @Override
    public Component getParent() {
        return parent;
    }

    @Override
    public void setParent(Component parent) {
        this.parent = parent;
    }

    /**
     * This method add a connection to the DEVS component.
     *
     * @param cFrom Component at the beginning of the connection
     * @param oPortIndex Index of the source port in cFrom, starting at 0
     * @param cTo Component at the end of the connection
     * @param iPortIndex Index of the destination port in cTo, starting at 0
     */
    public void addCoupling(Component cFrom, int oPortIndex, Component cTo, int iPortIndex) {
        if (cFrom == this) { // EIC
            Port portFrom = cFrom.inPorts.get(oPortIndex);
            Port portTo = cTo.inPorts.get(iPortIndex);
            Coupling coupling = new Coupling(portFrom, portTo);
            eic.add(coupling);
        } else if (cTo == this) { // EOC
            Port portFrom = cFrom.outPorts.get(oPortIndex);
            Port portTo = cTo.outPorts.get(iPortIndex);
            Coupling coupling = new Coupling(portFrom, portTo);
            eoc.add(coupling);
        } else { // IC
            Port portFrom = cFrom.outPorts.get(oPortIndex);
            Port portTo = cTo.inPorts.get(iPortIndex);
            Coupling coupling = new Coupling(portFrom, portTo);
            ic.add(coupling);
        }
    }

    @Override
    public void addInPort(Port<?> port) {
        super.addInPort(port);
        port.direction = Port.Direction.INOUT;
    }

    @Override
    public void addOutPort(Port<?> port) {
        super.addOutPort(port);
        port.direction = Port.Direction.INOUT;
    }

    /**
     * @deprecated This method add a connection to the DEVS component. This
     * method is deprecated because since the addition of the
     * <code>parent</code> attribute, both components <code>cFrom</code> and
     * <code>cTo</code> are no longer needed inside the Coupling class.
     * @param cFrom Component at the beginning of the connection
     * @param pFrom Port at the beginning of the connection
     * @param cTo Component at the end of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(Component cFrom, Port<?> pFrom, Component cTo, Port<?> pTo) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Coupling coupling = new Coupling(pFrom, pTo);
        // Add to connections
        if (cFrom == this) {
            eic.add(coupling);
        } else if (cTo == this) {
            eoc.add(coupling);
        } else {
            ic.add(coupling);
        }
    }

    public final void addCoupling(String cFromName, String pFromName, String cToName, String pToName) {
        Component cFrom = this.getComponentByName(cFromName);
        Component cTo = this.getComponentByName(cToName);
        if (cFrom == null) {
            LOGGER.severe(cFromName + " does not exist");
            return;
        }
        if (cTo == null) {
            LOGGER.severe(cToName + " does not exist");
            return;
        }
        Port<?> pFrom, pTo;
        // Add to connections
        if (cFrom == this) {
            pFrom = cFrom.getInPort(pFromName);
            pTo = cTo.getInPort(pToName);
        } else if (cTo == this) {
            pFrom = cFrom.getOutPort(pFromName);
            pTo = cTo.getOutPort(pToName);
        } else {
            pFrom = cFrom.getOutPort(pFromName);
            pTo = cTo.getInPort(pToName);
        }
        if (pFrom == null) {
            LOGGER.severe(cFrom.getName() + "::" + pFromName + "->" + cTo.getName() + "::" + pToName + " --> port " + pFromName + " at component " + cFrom.getName() + " does not exist");
            return;
        }
        if (pTo == null) {
            LOGGER.severe(cFrom.getName() + "::" + pFromName + "->" + cTo.getName() + "::" + pToName + " --> port " + pToName + " at component " + cTo.getName() + " does not exist");
            return;
        }
        this.addCoupling(pFrom, pTo);
    }

    /**
     * This member adds a connection between ports pFrom and pTo
     *
     * @param pFrom Port at the beginning of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(Port<?> pFrom, Port<?> pTo) {                
        if (pFrom.getParent() == null) {
            LOGGER.severe("Port " + pFrom.getName() + " does not have a parent component. Maybe the port was not added to the component?");
            return;
        }
        if (pTo.getParent() == null) {
            LOGGER.severe("Port " + pTo.getName() + " does not have a parent component. Maybe the port was not added to the component?");
            return;
        }
        Coupling coupling = new Coupling(pFrom, pTo);
        // Add to connections
        if (pFrom.getParent() == this) {
            eic.add(coupling);
        } else if (pTo.getParent() == this) {
            eoc.add(coupling);
        } else {
            ic.add(coupling);
        }
    }

    public Collection<Component> getComponents() {
        return components;
    }

    /**
     * Get the first component (including this coupled model) whose name match
     * with name argument.
     *
     * @param name The name of the component to find
     * @return The component, which name is equal to the argument. If no
     * component is found, null is returned.
     */
    public Component getComponentByName(String name) {
        if (this.name.equals(name)) {
            return this;
        }

        for (Component component : components) {
            if (component.name.equals(name)) {
                return component;
            }
        }

        return null;
    }

    public final void addComponent(Component component) {
        component.setParent(this);
        components.add(component);
    }

    public LinkedList<Coupling<?>> getIC() {
        return ic;
    }

    public LinkedList<Coupling<?>> getEIC() {
        return eic;
    }

    public LinkedList<Coupling<?>> getEOC() {
        return eoc;
    }

    @Override
    public void toChain() throws Exception {
        for (Component component: components) {
            component.toChain();
        }
        super.toChain();

        for (Coupling<?> coupling: ic) {
            chainPorts(coupling);
        }
        for (Coupling<?> coupling: eic) {
            chainPorts(coupling);
        }
        for (Coupling<?> coupling: eoc) {
            chainPorts(coupling);
        }
    }

    public static void chainPorts(Coupling<?> coupling) throws Exception {
        coupling.portFrom.addCouplingOut(coupling);
        coupling.portTo.addCouplingIn(coupling);
    }

    public void in2Out() {
        for (Port<?> port: inPorts) {
            port.direction = Port.Direction.OUT;
        }
    }

    /**
     * @return The new DEVS coupled model
     */
    public Coupled flatten() {
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (component instanceof Coupled) {
                ((Coupled) component).flatten();
                removePortsAndCouplings(component);
                components.remove(i--);
            }
        }

        if (parent == null) {
            return this;
        }

        // Process if parent ...
        // First, we store all the parent ports connected to input ports
        HashMap<Port<?>, LinkedList<Port<?>>> leftBridgeEIC = createLeftBrige(((Coupled) parent).getEIC());
        HashMap<Port<?>, LinkedList<Port<?>>> leftBridgeIC = createLeftBrige(((Coupled) parent).getIC());
        // The same with the output ports
        HashMap<Port<?>, LinkedList<Port<?>>> rightBridgeEOC = createRightBrige(((Coupled) parent).getEOC());
        HashMap<Port<?>, LinkedList<Port<?>>> rightBridgeIC = createRightBrige(((Coupled) parent).getIC());

        completeLeftBridge(eic, leftBridgeEIC, ((Coupled) parent).getEIC());
        completeLeftBridge(eic, leftBridgeIC, ((Coupled) parent).getIC());
        completeRightBridge(eoc, rightBridgeEOC, ((Coupled) parent).getEOC());
        completeRightBridge(eoc, rightBridgeIC, ((Coupled) parent).getIC());

        components.forEach((component) -> {
            ((Coupled) parent).addComponent(component);
        });

        ic.forEach((cIC) -> {
            ((Coupled) parent).getIC().add(cIC);
        });
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void completeLeftBridge(LinkedList<Coupling<?>> couplings,
            HashMap<Port<?>, LinkedList<Port<?>>> leftBridge,
            LinkedList<Coupling<?>> pCouplings) {
        for (Coupling<?> c : couplings) {
            LinkedList<Port<?>> list = leftBridge.get(c.portFrom);
            if (list != null) {
                for (Port<?> port : list) {
                    pCouplings.add(new Coupling(port, c.portTo));
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void completeRightBridge(LinkedList<Coupling<?>> couplings,
            HashMap<Port<?>, LinkedList<Port<?>>> rightBridge,
            LinkedList<Coupling<?>> pCouplings) {
        for (Coupling<?> c : couplings) {
            LinkedList<Port<?>> list = rightBridge.get(c.portTo);
            if (list != null) {
                for (Port<?> port : list) {
                    pCouplings.add(new Coupling(c.portFrom, port));
                }
            }
        }
    }

    private HashMap<Port<?>, LinkedList<Port<?>>> createLeftBrige(LinkedList<Coupling<?>> couplings) {
        HashMap<Port<?>, LinkedList<Port<?>>> leftBridge = new HashMap<>();
        for (Port<?> iPort : this.inPorts) {
            for (Coupling<?> c : couplings) {
                if (c.portTo == iPort) {
                    LinkedList<Port<?>> list = leftBridge.get(iPort);
                    if (list == null) {
                        list = new LinkedList<>();
                        leftBridge.put(iPort, list);
                    }
                    list.add(c.portFrom);
                }
            }
        }
        return leftBridge;
    }

    private HashMap<Port<?>, LinkedList<Port<?>>> createRightBrige(LinkedList<Coupling<?>> couplings) {
        HashMap<Port<?>, LinkedList<Port<?>>> rightBridge = new HashMap<>();
        for (Port<?> oPort : this.outPorts) {
            for (Coupling<?> c : couplings) {
                if (c.portFrom == oPort) {
                    LinkedList<Port<?>> list = rightBridge.get(oPort);
                    if (list == null) {
                        list = new LinkedList<>();
                        rightBridge.put(oPort, list);
                    }
                    list.add(c.portTo);
                }
            }
        }
        return rightBridge;
    }

    private void removePortsAndCouplings(Component child) {
        Collection<Port<?>> inPorts = child.getInPorts();
        for (Port<?> iport : inPorts) {
            for (int j = 0; j < eic.size(); ++j) {
                Coupling<?> c = eic.get(j);
                if (c.portTo == iport) {
                    eic.remove(j--);
                }
            }
            for (int j = 0; j < ic.size(); ++j) {
                Coupling<?> c = ic.get(j);
                if (c.portTo == iport) {
                    ic.remove(j--);
                }
            }
        }
        Collection<Port<?>> outPorts = child.getOutPorts();
        for (Port<?> oport : outPorts) {
            for (int j = 0; j < eoc.size(); ++j) {
                Coupling<?> c = eoc.get(j);
                if (c.portFrom == oport) {
                    eoc.remove(j--);
                }
            }
            for (int j = 0; j < ic.size(); ++j) {
                Coupling<?> c = ic.get(j);
                if (c.portFrom == oport) {
                    ic.remove(j--);
                }
            }
        }
    }

}
