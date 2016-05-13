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
package xdevs.core.simulation.parallel;

import java.util.concurrent.Callable;
import xdevs.core.simulation.AbstractSimulator;

/**
 *
 * @author jlrisco
 */
public class TaskLambda implements Callable<Double> {
    
    protected AbstractSimulator simulator;
    
    public TaskLambda(AbstractSimulator simulator) {
        this.simulator = simulator;
    }

    @Override
    public Double call() {
        simulator.lambda();
        return simulator.getClock().getTime();
    }
}
