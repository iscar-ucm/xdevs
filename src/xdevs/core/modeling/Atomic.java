package xdevs.core.modeling;

import xdevs.core.Constants;
import xdevs.core.modeling.api.AtomicInterface;

/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public abstract class Atomic extends Component implements AtomicInterface {

    // DevsAtomic attributes
    protected String phase = Constants.PHASE_PASSIVE;
    protected double sigma = Constants.INFINITY;

    public Atomic(String name) {
        super(name);
    }

    public Atomic() {
        this(Atomic.class.getSimpleName());
    }
    // DevsAtomic methods

    @Override
    public double ta() {
        return sigma;
    }

    @Override
    public void deltcon(double e) {
        deltint();
        deltext(0);
    }

    @Override
    public void holdIn(String phase, double sigma) {
        this.phase = phase;
        this.sigma = sigma;
    }

    @Override
    public void activate() {
        this.phase = Constants.PHASE_ACTIVE;
        this.sigma = 0;
    }

    @Override
    public void passivate() {
        this.phase = Constants.PHASE_PASSIVE;
        this.sigma = Constants.INFINITY;
    }

    @Override
    public void passivateIn(String phase) {
        this.phase = phase;
        this.sigma = Constants.INFINITY;
    }

    @Override
    public boolean phaseIs(String phase) {
        return this.phase.equals(phase);
    }

    @Override
    public String getPhase() {
        return phase;
    }

    @Override
    public void setPhase(String phase) {
        this.phase = phase;
    }

    @Override
    public double getSigma() {
        return sigma;
    }

    @Override
    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    @Override
    public String showState() {
        StringBuilder sb = new StringBuilder(name + ":[");
        sb.append("\tstate: " + phase);
        sb.append("\t, sigma: " + sigma);
        sb.append("]");
        return sb.toString();
    }

}
