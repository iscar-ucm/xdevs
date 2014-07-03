package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Ef extends Coupled {

  protected Port<Job> iIn = new Port<Job>();
  protected Port<Job> oOut = new Port<Job>();

  public Ef(double period, double observationTime) {
    super.addInPort(iIn);
    super.addOutPort(oOut);
    Generator generator = new Generator(period);
    addComponent(generator);
    Transducer transducer = new Transducer(observationTime);
    addComponent(transducer);
    addCoupling(this, this.iIn, transducer, transducer.iSolved);
    addCoupling(generator, generator.oOut, this, this.oOut);
    addCoupling(generator, generator.oOut, transducer, transducer.iArrived);
    addCoupling(transducer, transducer.oOut, generator, generator.iStop);
  }
}
