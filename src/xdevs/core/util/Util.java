package xdevs.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import xdevs.core.modeling.api.CoupledInterface;
import xdevs.core.modeling.api.PortInterface;

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
    public static String printBridge(String prefix, HashMap<PortInterface, LinkedList<PortInterface>> bridge) {
        StringBuilder sb = new StringBuilder(prefix);
        for (PortInterface port : bridge.keySet()) {
            LinkedList<PortInterface> ports = bridge.get(port);
            sb.append("{").append(port.toString()).append("(");
            Iterator<?> it = ports.iterator();
            while (it.hasNext()) {
                sb.append(it.next()).append(" ");
            }
            sb.append(")}");
        }
        return sb.toString();

    }

    public static String printCouplings(CoupledInterface model) {
        StringBuilder sb = new StringBuilder(" coupling: [");
        sb.append(printLinkedList("\n\tEIC", model.getEIC()));
        sb.append(printLinkedList("\n\tIC", model.getIC()));
        sb.append(printLinkedList("\n\tEOC", model.getEOC()));
        sb.append("\n\t]");
        return sb.toString();
    }
}
