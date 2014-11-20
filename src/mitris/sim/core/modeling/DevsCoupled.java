package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José Luis Risco Martín
 */
public interface DevsCoupled extends Component {
    public void addCoupling(Component cFrom, Port<?> pFrom, Component cTo, Port<?> pTo);    
    public Collection<Component> getComponents();
    public void addComponent(Component component);
    public LinkedList<Coupling<?>> getIC();
    public LinkedList<Coupling<?>> getEIC();
    public LinkedList<Coupling<?>> getEOC();
    /**
     * This is a difficult decision. Where should be place this function, in the interface or just in the implementation? 
     * Think about it.
     * @param parent Coupled model of the one we want to flatten
     * @return The new DEVS coupled model
     */
	public DevsCoupled flatten(DevsCoupled parent);
}
