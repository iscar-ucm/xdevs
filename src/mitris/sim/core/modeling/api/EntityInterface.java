package mitris.sim.core.modeling.api;

/**
 * 
 * Class that implements the basic entity needed to manage all the DEVS elements.
 * An entity is a component, atomic model, coupled model, connection, port ... everything.
 * @author José Luis Risco Martín
 * @author Saurabh Mittal
 *
 */
public interface EntityInterface {
	/**
	 * Member funcion to get the name of the entity
	 * @return The name of this entity.
	 */
	public String getName();
	
	/**
	 * Qualified name
	 * @return the qualified name of the given entity
	 */
	public String getQualifiedName(); 
}
