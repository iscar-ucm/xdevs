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
