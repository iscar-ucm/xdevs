/*
 * Copyright (C) 2014-2016 José Luis Risco Martín <jlrisco@ucm.es>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.lib.performance;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

/**
 * Coupled model to study the performance using DEVStone
 *
 * @author José Luis Risco Martín
 */
public abstract class DevStone extends Coupled {
    public Port<Integer> iIn = new Port<>("in");
    public Port<Integer> oOut = new Port<>("out");

    public DevStone(String name) {
        super(name);
        super.addInPort(iIn);
        super.addOutPort(oOut);
    }

    public abstract int getNumOfAtomic(int width, int depth);

    public abstract int getNumDeltExts(int maxEvents, int width, int depth);

    public abstract int getNumDeltInts(int maxEvents, int width, int depth);

    public abstract long getNumOfEvents(int maxEvents, int width, int depth);
}
