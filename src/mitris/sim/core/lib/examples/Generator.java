/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Generator extends Atomic {

  protected Port<Job> iStop = new Port<Job>();
  protected Port<Job> oOut = new Port<Job>();
  protected int jobCounter;
  protected double period;

  public Generator(double period) {
    super.addInPort(iStop);
    super.addOutPort(oOut);
    this.period = period;
    jobCounter = 1;
    this.holdIn("active", period);
  }

  @Override
  public void deltint() {
    jobCounter++;
    this.holdIn("active", period);
  }

  @Override
  public void deltext(double e) {
    super.passivate();
  }

  @Override
  public void lambda() {
    Job job = new Job("" + jobCounter + "");
    oOut.addValue(job);
  }
}
