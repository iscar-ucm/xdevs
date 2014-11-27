/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.lib.examples;

import lombok.Data;
import mitris.sim.core.modeling.EntityBase;

/**
 *
 * @author jlrisco
 */
@Data
public class Job extends EntityBase{

  protected String id;
  protected double time;

  public Job(String name) {
    this.id = name;
    this.time = 0.0;
  }
}
