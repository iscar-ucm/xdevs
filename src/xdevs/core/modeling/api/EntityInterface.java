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
package xdevs.core.modeling.api;

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
