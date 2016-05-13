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
package xdevs.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.Port;

/**
 * @author Saurabh Mittal
 * @author José Luis Risco Martín
 *
 */
public class Util {

    public static String printLinkedList(String prefix, LinkedList<?> list) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = list.iterator();
        sb.append(prefix).append(" [");
        while (it.hasNext()) {
            sb.append(it.next().toString());
        }
        sb.append("]");
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    public static String printBridge(String prefix, HashMap<Port, LinkedList<Port>> bridge) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Port port : bridge.keySet()) {
            LinkedList<Port> ports = bridge.get(port);
            sb.append("{").append(port.toString()).append("(");
            Iterator<?> it = ports.iterator();
            while (it.hasNext()) {
                sb.append(it.next()).append(" ");
            }
            sb.append(")}");
        }
        return sb.toString();

    }

    public static String printCouplings(Coupled model) {
        StringBuilder sb = new StringBuilder(" coupling: [");
        sb.append(printLinkedList("\n\tEIC", model.getEIC()));
        sb.append(printLinkedList("\n\tIC", model.getIC()));
        sb.append(printLinkedList("\n\tEOC", model.getEOC()));
        sb.append("\n\t]");
        return sb.toString();
    }
}
