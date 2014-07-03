package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author José L. Risco-Martín and Saurabh Mittal
 */
@SuppressWarnings("rawtypes")
public abstract class Component {

    protected LinkedList<Port> inPorts = new LinkedList<>();
    protected LinkedList<Port> outPorts = new LinkedList<>();

    public boolean isInputEmpty() {
        for (Port port : inPorts) {
            if (!port.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void addInPort(Port port) {
        inPorts.add(port);
    }

    public Collection<Port> getInPorts() {
        return inPorts;
    }

    public void addOutPort(Port port) {
        outPorts.add(port);
    }

    public Collection<Port> getOutPorts() {
        return outPorts;
    }
}
