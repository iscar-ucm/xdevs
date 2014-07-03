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
    
    public Coupled flatten() {
        Collection<Component> components = this.getComponents();
        for(Component component : components) {
            if(component instanceof Coupled) {
                ((Coupled)component).flatten();
            }
        }
        return this;
    }
}
