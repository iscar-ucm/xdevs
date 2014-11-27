/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.modeling.api;


/**
 *
 * @author José L. Risco Martín and Saurabh Mittal
 */
public interface DevsAtomic extends Component {
    public double ta();
    public void deltint();
    public void deltext(double e);
    public void deltcon(double e);
    public void lambda();
    public void holdIn(String phase, double sigma);
    public void activate();
    public void passivate();
    public void passivateIn(String phase);
    public boolean phaseIs(String phase);
    public String getPhase();
    public void setPhase(String phase);
    public double getSigma();
    public void setSigma(double sigma);
}
