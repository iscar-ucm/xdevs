/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling;

import mitris.sim.core.Constants;

/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public abstract class Atomic extends Component {

    protected String phase;
    protected double sigma;

    public Atomic(String name) {
    	super(name);
        phase = Constants.PHASE_PASSIVE;
        sigma = Constants.INFINITY;
    }
    
    public double ta() {
        return sigma;
    }

    abstract public void deltint();

    abstract public void deltext(double e);

    public void deltcon(double e) {
        deltint();
        deltext(0);
    }

    abstract public void lambda();
    
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
