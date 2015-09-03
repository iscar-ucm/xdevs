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

import java.util.Collection;
import java.util.LinkedList;

import xdevs.core.modeling.Coupling;

/**
 *
 * @author José Luis Risco Martín
 */
public interface CoupledInterface extends ComponentInterface {

    /**
     * @deprecated This method add a connection to the DEVS component. This
     * method is deprecated because since the addition of the
     * <code>parent</code> attribute, both components <code>cFrom</code> and
     * <code>cTo</code> are no longer needed.
     * @param cFrom Component at the beginning of the connection
     * @param pFrom Port at the beginning of the connection
     * @param cTo Component at the end of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(ComponentInterface cFrom, PortInterface<?> pFrom, ComponentInterface cTo, PortInterface<?> pTo);

    /**
     * This member adds a connection between ports pFrom and pTo
     *
     * @param pFrom Port at the beginning of the connection
     * @param pTo Port at the end of the connection
     */
    public void addCoupling(PortInterface<?> pFrom, PortInterface<?> pTo);

    public Collection<ComponentInterface> getComponents();

    public void addComponent(ComponentInterface component);

    public LinkedList<Coupling<?>> getIC();

    public LinkedList<Coupling<?>> getEIC();

    public LinkedList<Coupling<?>> getEOC();

    /**
     * This is a difficult decision. Where should be place this function, in the
     * interface or just in the implementation? Think about it.
     *
     * @return The new DEVS coupled model
     */
    public CoupledInterface flatten();
}
