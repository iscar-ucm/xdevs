/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.simulation.api;

import mitris.sim.core.modeling.api.ComponentInterface;

/**
 *
 * @author jlrisco
 */
public interface SimulatorInterface {

    public void initialize();

    public double ta();

    public void lambda();

    public void deltfcn();

    public void clear();

    public double getTL();

    public void setTL(double tL);

    public double getTN();

    public void setTN(double tN);

    public SimulationClock getClock();

    public ComponentInterface getModel();
}
