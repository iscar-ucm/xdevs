/**
 * 
 */
package mitris.sim.core.modeling;

import mitris.sim.core.modeling.api.Entity;

/**
 * @author smittal
 *
 */
public class EntityBase implements Entity {

	protected String name;
	
	public EntityBase(){
		this("Entity");
	}
	
	public EntityBase(String name){
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
