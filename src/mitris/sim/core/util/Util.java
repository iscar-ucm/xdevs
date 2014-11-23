package mitris.sim.core.util;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import mitris.sim.core.modeling.api.DevsCoupled;
import mitris.sim.core.modeling.api.Port;

/**
 * @author Saurabh Mittal
 * @author José Luis Risco Martín
 *
 */
public class Util {

	public static String printLinkedList(String prefix, LinkedList<?> list){
		StringBuilder sb = new StringBuilder();
		Iterator<?> it = list.iterator();
		sb.append(prefix + " [");
		while(it.hasNext()){
			sb.append(it.next().toString());
		}
		sb.append("]");
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	public static String printBridge(String prefix, HashMap<Port, LinkedList<Port>> bridge){
		StringBuilder sb = new StringBuilder(prefix);
		for(Port port: bridge.keySet()){
			LinkedList<Port> ports = bridge.get(port);
			sb.append("{"+port+"(");
			Iterator<?> it = ports.iterator();
			while(it.hasNext()){
				sb.append(it.next()+" ");
			}
			sb.append(")}");
		}
		return sb.toString();

	}

	public static String printCouplings(DevsCoupled model){
		StringBuilder sb = new StringBuilder(" coupling: [");
		sb.append(printLinkedList("\n\tEIC", model.getEIC()));
		sb.append(printLinkedList("\n\tIC", model.getIC()));
		sb.append(printLinkedList("\n\tEOC", model.getEOC()));
		sb.append("\n\t]");
		return sb.toString();         
	}
}
