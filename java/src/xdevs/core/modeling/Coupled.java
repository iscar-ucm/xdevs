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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coupled extends Component {

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

   /**
     * This member adds a connection between ports pFrom and pTo
     *
     * @param pFrom Port at the beginning of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(Port<?> pFrom, Port<?> pTo) {
        @SuppressWarnings({"rawtypes", "unchecked"})
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

    public void addComponent(Component component) {
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

        for (Component component : components) {
            ((Coupled) parent).addComponent(component);
        }

        for (Coupling<?> cIC : ic) {
            ((Coupled) parent).getIC().add(cIC);
        }
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
