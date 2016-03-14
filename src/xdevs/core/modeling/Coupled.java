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

import xdevs.core.modeling.api.ComponentInterface;
import xdevs.core.modeling.api.CoupledInterface;
import xdevs.core.modeling.api.PortInterface;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coupled extends Component implements CoupledInterface {

    // Coupled attributes
    protected LinkedList<ComponentInterface> components = new LinkedList<>();
    protected LinkedList<Coupling<?>> ic = new LinkedList<>();
    protected LinkedList<Coupling<?>> eic = new LinkedList<>();
    protected LinkedList<Coupling<?>> eoc = new LinkedList<>();

    public Coupled(String name) {
        this.name = name;
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
    public ComponentInterface getParent() {
        return parent;
    }

    @Override
    public void setParent(ComponentInterface parent) {
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
     * Coupled members	/** Coupled members
     */
    @Override
    public void addCoupling(ComponentInterface cFrom, PortInterface<?> pFrom, ComponentInterface cTo, PortInterface<?> pTo) {
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

    @Override
    public void addCoupling(PortInterface<?> pFrom, PortInterface<?> pTo) {
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

    @Override
    public Collection<ComponentInterface> getComponents() {
        return components;
    }

    @Override
    public void addComponent(ComponentInterface component) {
        component.setParent(this);
        components.add(component);
    }

    @Override
    public LinkedList<Coupling<?>> getIC() {
        return ic;
    }

    @Override
    public LinkedList<Coupling<?>> getEIC() {
        return eic;
    }

    @Override
    public LinkedList<Coupling<?>> getEOC() {
        return eoc;
    }

    @Override
    public CoupledInterface flatten() {
        for (int i = 0; i < components.size(); ++i) {
            ComponentInterface component = components.get(i);
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
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> leftBridgeEIC = createLeftBrige(((CoupledInterface) parent).getEIC());
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> leftBridgeIC = createLeftBrige(((CoupledInterface) parent).getIC());
        // The same with the output ports
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> rightBridgeEOC = createRightBrige(((CoupledInterface) parent).getEOC());
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> rightBridgeIC = createRightBrige(((CoupledInterface) parent).getIC());

        completeLeftBridge(eic, leftBridgeEIC, ((CoupledInterface) parent).getEIC());
        completeLeftBridge(eic, leftBridgeIC, ((CoupledInterface) parent).getIC());
        completeRightBridge(eoc, rightBridgeEOC, ((CoupledInterface) parent).getEOC());
        completeRightBridge(eoc, rightBridgeIC, ((CoupledInterface) parent).getIC());

        for (ComponentInterface component : components) {
            ((CoupledInterface) parent).addComponent(component);
        }

        for (Coupling<?> cIC : ic) {
            ((CoupledInterface) parent).getIC().add(cIC);
        }
        return this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void completeLeftBridge(LinkedList<Coupling<?>> couplings,
            HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> leftBridge,
            LinkedList<Coupling<?>> pCouplings) {
        for (Coupling<?> c : couplings) {
            LinkedList<PortInterface<?>> list = leftBridge.get(c.portFrom);
            if (list != null) {
                for (PortInterface<?> port : list) {
                    pCouplings.add(new Coupling(port, c.portTo));
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void completeRightBridge(LinkedList<Coupling<?>> couplings,
            HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> rightBridge,
            LinkedList<Coupling<?>> pCouplings) {
        for (Coupling<?> c : couplings) {
            LinkedList<PortInterface<?>> list = rightBridge.get(c.portTo);
            if (list != null) {
                for (PortInterface<?> port : list) {
                    pCouplings.add(new Coupling(c.portFrom, port));
                }
            }
        }
    }

    private HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> createLeftBrige(LinkedList<Coupling<?>> couplings) {
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> leftBridge = new HashMap<>();
        for (PortInterface<?> iPort : this.inPorts) {
            for (Coupling<?> c : couplings) {
                if (c.portTo == iPort) {
                    LinkedList<PortInterface<?>> list = leftBridge.get(iPort);
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

    private HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> createRightBrige(LinkedList<Coupling<?>> couplings) {
        HashMap<PortInterface<?>, LinkedList<PortInterface<?>>> rightBridge = new HashMap<>();
        for (PortInterface<?> oPort : this.outPorts) {
            for (Coupling<?> c : couplings) {
                if (c.portFrom == oPort) {
                    LinkedList<PortInterface<?>> list = rightBridge.get(oPort);
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

    private void removePortsAndCouplings(ComponentInterface child) {
        Collection<InPort<?>> inPorts = child.getInPorts();
        for (PortInterface<?> iport : inPorts) {
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
        Collection<OutPort<?>> outPorts = child.getOutPorts();
        for (OutPort<?> oport : outPorts) {
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
