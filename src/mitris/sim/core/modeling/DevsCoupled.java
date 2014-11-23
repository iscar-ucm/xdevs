package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José Luis Risco Martín
 */
public interface DevsCoupled extends Component {
	/**
	 * @deprecated This method add a connection to the DEVS component. This method is deprecated because
	 * since the addition of the <code>parent</code> attribute, both components <code>cFrom</code> and 
	 * <code>cTo</code> are no longer needed.
	 * @param cFrom Component at the beginning of the connection
	 * @param pFrom Port at the beginning of the connection
	 * @param cTo Component at the end of the connection
	 * @param pTo Port at the end of the connection
	 */
    public void addCoupling(Component cFrom, Port<?> pFrom, Component cTo, Port<?> pTo);    
    /**
     * This member adds a connection between ports pFrom and pTo
     * @param pFrom Port at the beginning of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(Port<?> pFrom, Port<?> pTo);
    public Collection<Component> getComponents();
    public void addComponent(Component component);
    public LinkedList<Coupling<?>> getIC();
    public LinkedList<Coupling<?>> getEIC();
    public LinkedList<Coupling<?>> getEOC();
    /**
     * This is a difficult decision. Where should be place this function, in the interface or just in the implementation? 
     * Think about it.
     * @return The new DEVS coupled model
     */
	public DevsCoupled flatten();
}
