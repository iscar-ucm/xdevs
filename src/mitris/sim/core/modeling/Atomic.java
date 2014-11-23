/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import java.util.Collection;
import java.util.LinkedList;

import mitris.sim.core.Constants;
import mitris.sim.core.modeling.api.DevsAtomic;
import mitris.sim.core.modeling.api.DevsCoupled;

/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public abstract class Atomic implements DevsAtomic {

	// Entity attributes
	protected String name = Atomic.class.getSimpleName();
	// Component attributes
	protected DevsCoupled parent = null;
	protected LinkedList<InPort<?>> inPorts = new LinkedList<>();
	protected LinkedList<OutPort<?>> outPorts = new LinkedList<>();
	// DevsAtomic attributes
    protected String phase = Constants.PHASE_PASSIVE;
    protected double sigma = Constants.INFINITY;

    public Atomic(String name) {
    	this.name = name;
        phase = Constants.PHASE_PASSIVE;
        sigma = Constants.INFINITY;
    }
    
    public Atomic() {
    	this(Atomic.class.getSimpleName());
    }
    
	public void initialize() {
        phase = Constants.PHASE_PASSIVE;
        sigma = Constants.INFINITY;		
	}
    
    // Entity methods
    public String getName() {
    	return name;
    }
    
    public String toString(){
		StringBuilder sb = new StringBuilder(name + " :");
		sb.append(" Inports[ ");
		for(InPort<?> p : inPorts){
			sb.append(p + " ");
		}
		sb.append("]");
		sb.append(" Outports[ ");
		for(OutPort<?> p: outPorts){
			sb.append(p+" ");
		}
		sb.append("]");
		return sb.toString();
	}
    
    // Component methods
	public boolean isInputEmpty() {
		for (InPort<?> port : inPorts) {
			if (!port.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public void addInPort(InPort<?> port) {
		inPorts.add(port);
		port.parent = this;
	}

	public Collection<InPort<?>> getInPorts() {
		return inPorts;
	}

	public void addOutPort(OutPort<?> port) {
		outPorts.add(port);
		port.parent = this;
	}

	public Collection<OutPort<?>> getOutPorts() {
		return outPorts;
	}

	public DevsCoupled getParent() {
		return parent;
	}
	
	public void setParent(DevsCoupled parent) {
		this.parent = parent;
	}
    
    // DevsAtomic methods
    
    public double ta() {
        return sigma;
    }

    public void deltcon(double e) {
        deltint();
        deltext(0);
    }

    public void holdIn(String phase, double sigma) {
        this.phase = phase;
        this.sigma = sigma;
    }

    public void activate() {
        this.phase = Constants.PHASE_ACTIVE;
        this.sigma = 0;
    }

    public void passivate() {
        this.phase = Constants.PHASE_PASSIVE;
        this.sigma = Constants.INFINITY;
    }
    
    public void passivateIn(String phase) {
        this.phase = phase;
        this.sigma = Constants.INFINITY;    	
    }

    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }
}
