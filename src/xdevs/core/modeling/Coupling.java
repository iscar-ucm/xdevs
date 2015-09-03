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

import xdevs.core.modeling.api.PortInterface;


/**
 * TODO: Saurabh wants and interface for Coupling. I find no reason to create such interface. There is only
 * one kind of Coupling (for now), so no interfaces are needed.
 * @author José Luis Risco Martín
 */
public class Coupling<E> {

	protected PortInterface<E> portFrom;
	protected PortInterface<E> portTo;

	public Coupling(PortInterface<E> portFrom, PortInterface<E> portTo) {
		this.portFrom = portFrom;
		this.portTo = portTo;
	}

	public String toString(){
		return "(" + portFrom + "->" + portTo+")";
	}

	// Coupling members
	
	public void propagateValues() {
		portTo.addValues(portFrom.getValues());
	}

	public PortInterface<E> getPortFrom() {
		return portFrom;
	}

	public PortInterface<E> getPortTo() {
		return portTo;
	}
}
