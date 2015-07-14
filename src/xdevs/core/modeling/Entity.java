/**
 *
 */
package xdevs.core.modeling;

import xdevs.core.modeling.api.EntityInterface;

/**
 * @author smittal
 *
 */
public class Entity implements EntityInterface {

    protected String name;

    public Entity() {
        this("Entity");
    }

    public Entity(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    /* 
     * @see mitris.sim.core.modeling.api.EntityInterface#getName()
     */

    @Override
    public String getName() {
        return name;
    }

    /* 
     * @see mitris.sim.core.modeling.api.EntityInterface#getQualifiedName()
     */
    @Override
    public String getQualifiedName() {
        return name;
    }

}
