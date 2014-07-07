/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author jlrisco
 */
public class Coupled extends Component {

    protected LinkedList<Component> components = new LinkedList<>();
    // Internal connections, external input connections and external output connections
    protected LinkedList<Coupling> ic = new LinkedList<>();
    protected LinkedList<Coupling> eic = new LinkedList<>();
    protected LinkedList<Coupling> eoc = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    public void addCoupling(Component cFrom, Port pFrom, Component cTo, Port pTo) {
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

    public Collection<Component> getComponents() {
        return components;
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    public LinkedList<Coupling> getIC() {
        return ic;
    }

    public LinkedList<Coupling> getEIC() {
        return eic;
    }

    public LinkedList<Coupling> getEOC() {
        return eoc;
    }

    public Coupled flatten(Coupled parent) {
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (component instanceof Coupled) {
                ((Coupled) component).flatten(this);
                removePortsAndCouplings(component);
                components.remove(i--);
            }
        }

        if (parent == null) {
            return this;
        }

        // Process if parent ...
        // First, we store all the parent ports connected to input ports
        HashMap<Port, LinkedList<Port>> leftBridgeEIC = createLeftBrige(parent.eic);
        HashMap<Port, LinkedList<Port>> leftBridgeIC = createLeftBrige(parent.ic);
        // The same with the output ports
        HashMap<Port, LinkedList<Port>> rightBridgeEOC = createRightBrige(parent.eoc);
        HashMap<Port, LinkedList<Port>> rightBridgeIC = createRightBrige(parent.ic);

        completeLeftBridge(eic, leftBridgeEIC, parent.eic);
        completeLeftBridge(eic, leftBridgeIC, parent.ic);
        completeRightBridge(eoc, rightBridgeEOC, parent.eoc);
        completeRightBridge(eoc, rightBridgeIC, parent.ic);

        for (Component component : components) {
            parent.addComponent(component);
        }

        for (Coupling cIC : ic) {
            parent.ic.add(cIC);
        }
        return this;
    }

    private void completeLeftBridge(LinkedList<Coupling> couplings,
            HashMap<Port, LinkedList<Port>> leftBridge,
            LinkedList<Coupling> pCouplings) {
        for (Coupling c : couplings) {
            LinkedList<Port> list = leftBridge.get(c.portFrom);
            if (list != null) {
                for (Port port : list) {
                    pCouplings.add(new Coupling(port, c.portTo));
                }
            }
        }
    }

    private void completeRightBridge(LinkedList<Coupling> couplings,
            HashMap<Port, LinkedList<Port>> rightBridge,
            LinkedList<Coupling> pCouplings) {
        for (Coupling c : couplings) {
            LinkedList<Port> list = rightBridge.get(c.portTo);
            if (list != null) {
                for (Port port : list) {
                    pCouplings.add(new Coupling(c.portFrom, port));
                }
            }
        }
    }

    private HashMap<Port, LinkedList<Port>> createLeftBrige(LinkedList<Coupling> couplings) {
        HashMap<Port, LinkedList<Port>> leftBridge = new HashMap<>();
        for (Port iPort : this.inPorts) {
            for (Coupling c : couplings) {
                if (c.portTo == iPort) {
                    LinkedList<Port> list = leftBridge.get(iPort);
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

    private HashMap<Port, LinkedList<Port>> createRightBrige(LinkedList<Coupling> couplings) {
        HashMap<Port, LinkedList<Port>> rightBridge = new HashMap<>();
        for (Port oPort : this.outPorts) {
            for (Coupling c : couplings) {
                if (c.portFrom == oPort) {
                    LinkedList<Port> list = rightBridge.get(oPort);
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
        for (Port iport : child.inPorts) {
            for (int j = 0; j < eic.size(); ++j) {
                Coupling c = eic.get(j);
                if (c.portTo == iport) {
                    eic.remove(j--);
                }
            }
            for (int j = 0; j < ic.size(); ++j) {
                Coupling c = ic.get(j);
                if (c.portTo == iport) {
                    ic.remove(j--);
                }
            }
        }
        for (Port oport : child.outPorts) {
            for (int j = 0; j < eoc.size(); ++j) {
                Coupling c = eoc.get(j);
                if (c.portFrom == oport) {
                    eoc.remove(j--);
                }
            }
            for (int j = 0; j < ic.size(); ++j) {
                Coupling c = ic.get(j);
                if (c.portFrom == oport) {
                    ic.remove(j--);
                }
            }
        }
    }
}
