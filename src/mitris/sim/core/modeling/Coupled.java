/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import java.util.Collection;
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
        /** TODO: This functions must be improved because:
         * (1) Two different couplings can be connected to same EIC input port
         * (2) The same EOC can be connected to two different models
         * (2) Some residual couplings must be deleted (after removing the child 
         * coupled model, remove also all the connections to/from it.
         */
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (component instanceof Coupled) {
                ((Coupled) component).flatten(this);
                components.remove(i--);
            }
        }

        if (parent != null) {
            LinkedList<Coupling> pEICs = parent.getEIC();
            LinkedList<Coupling> pICs = parent.getIC();
            LinkedList<Coupling> pEOCs = parent.getEOC();
            for (Coupling cEIC : eic) {
                for (int i=0; i<pEICs.size(); ++i) {
                    Coupling pEIC = pEICs.get(i);
                    if (pEIC.portTo == cEIC.portFrom) {
                        cEIC.portFrom = pEIC.portFrom;
                        pEICs.add(cEIC);
                    }
                }
                for (int i = 0; i < pICs.size(); ++i) {
                    Coupling pIC = pICs.get(i);
                    if (pIC.portTo == cEIC.portFrom) {
                        cEIC.portFrom = pIC.portFrom;
                        pICs.add(cEIC);
                    }
                }
            }

            for (Coupling cEOC : eoc) {
                for (int i=0; i<pEOCs.size(); ++i) {
                    Coupling pEOC = pEOCs.get(i);
                    if (pEOC.portFrom == cEOC.portTo) {
                        cEOC.portTo = pEOC.portTo;
                        pEOCs.add(cEOC);
                    }

                }
                for (int i = 0; i < pICs.size(); ++i) {
                    Coupling pIC = pICs.get(i);
                    if (pIC.portFrom == cEOC.portTo) {
                        cEOC.portTo = pIC.portTo;
                        pICs.add(cEOC);
                    }
                }
            }

            for (Component component : components) {
                parent.addComponent(component);
            }

            for (Coupling cIC : ic) {
                pICs.add(cIC);
            }
        }
        return this;
    }
}
