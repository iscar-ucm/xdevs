/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

/**
 *
 * @author jlrisco
 */
public class Job {

  protected String id;
  protected double time;

  public Job(String name) {
    this.id = name;
    this.time = 0.0;
  }
}
