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
 *  - José Luis Risco Martín <jlrisco@ucm.es>
 *  - Saurabh Mittal <smittal@duniptech.com>
 */
package xdevs.core.simulation.api;

import java.util.Collection;
import xdevs.core.modeling.InPort;

/**
 *
 * @author José Luis Risco Martín
 */
public interface CoordinatorInterface extends SimulatorInterface {

    public Collection<SimulatorInterface> getSimulators();

    public void propagateOutput();

    public void propagateInput();

    /**
     * Injects a value into the port "port", calling the transition function.
     * @param e elapsed time
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(double e, InPort port, Collection<Object> values);

    /**
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(0.0, InPort, Collection)
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(InPort port, Collection<Object> values);

    /**
     * Injects a single value in the given input port with elapsed time e.
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(double, InPort, Collection)
     * @param e
     * @param port
     * @param value
     */
    public void simInject(double e, InPort port, Object value);

    /**
     * Injects a single value in the given input port with elapsed time e equal to 0.
     * @see xdevs.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(0.0, InPort, Collection)
     * @param port
     * @param value
     */
    public void simInject(InPort port, Object value);

    public void simulate(long numIterations);

    public void simulate(double timeInterval);
}
