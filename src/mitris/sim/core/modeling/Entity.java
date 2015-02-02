/**
 * 
 */
package mitris.sim.core.modeling;

import mitris.sim.core.modeling.api.EntityInterface;

/**
 * @author smittal
 *
 */
public class Entity implements EntityInterface {

	protected String name;
	
	public Entity(){
		this("Entity");
	}
	
	public Entity(String name){
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	/* (non-Javadoc)
	 * @see mitris.sim.core.modeling.api.Entity#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see mitris.sim.core.modeling.api.Entity#getQualifiedName()
	 */
	public String getQualifiedName() {
		return name;
	}

}
