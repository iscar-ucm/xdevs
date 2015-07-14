/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.lib.examples;

import xdevs.core.modeling.Entity;

/**
 *
 * @author jlrisco
 */
public class Job extends Entity{

  protected String id;
  protected double time;

  public Job(String name) {
    this.id = name;
    this.time = 0.0;
  }
}
