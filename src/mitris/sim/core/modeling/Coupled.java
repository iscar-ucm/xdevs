package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import mitris.sim.core.modeling.api.Component;
import mitris.sim.core.modeling.api.DevsCoupled;
import mitris.sim.core.modeling.api.Port;

/**
 *
 * @author José Luis Risco Martín
 */
public class Coupled extends ComponentBase implements DevsCoupled {

	// Coupled attributes
	protected LinkedList<Component> components = new LinkedList<>();
	protected LinkedList<Coupling<?>> ic = new LinkedList<>();
	protected LinkedList<Coupling<?>> eic = new LinkedList<>();
	protected LinkedList<Coupling<?>> eoc = new LinkedList<>();

	public Coupled(String name) {
		this.name = name;
	}

	public Coupled() {
		this(Coupled.class.getSimpleName());
	}

	public void initialize() {
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
	 * Coupled members
	 */

	public void addCoupling(Component cFrom, Port<?> pFrom, Component cTo, Port<?> pTo) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
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

	public void addCoupling(Port<?> pFrom, Port<?> pTo) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
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

	public DevsCoupled flatten() {
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
		HashMap<Port<?>, LinkedList<Port<?>>> leftBridgeEIC = createLeftBrige(((DevsCoupled)parent).getEIC());
		HashMap<Port<?>, LinkedList<Port<?>>> leftBridgeIC = createLeftBrige(((DevsCoupled)parent).getIC());
		// The same with the output ports
		HashMap<Port<?>, LinkedList<Port<?>>> rightBridgeEOC = createRightBrige(((DevsCoupled)parent).getEOC());
		HashMap<Port<?>, LinkedList<Port<?>>> rightBridgeIC = createRightBrige(((DevsCoupled)parent).getIC());

		completeLeftBridge(eic, leftBridgeEIC, ((DevsCoupled)parent).getEIC());
		completeLeftBridge(eic, leftBridgeIC, ((DevsCoupled)parent).getIC());
		completeRightBridge(eoc, rightBridgeEOC, ((DevsCoupled)parent).getEOC());
		completeRightBridge(eoc, rightBridgeIC, ((DevsCoupled)parent).getIC());

		for (Component component : components) {
			((DevsCoupled)parent).addComponent(component);
		}

		for (Coupling<?> cIC : ic) {
			((DevsCoupled)parent).getIC().add(cIC);
		}
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		Collection<InPort<?>> inPorts = child.getInPorts();
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
