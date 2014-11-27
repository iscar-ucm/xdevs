/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import mitris.sim.core.Constants;
import mitris.sim.core.modeling.api.DevsAtomic;

/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public abstract class Atomic extends ComponentBase implements DevsAtomic {

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
