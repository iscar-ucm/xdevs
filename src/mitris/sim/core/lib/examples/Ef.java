package mitris.sim.core.lib.examples;

import mitris.sim.core.modeling.Coupled;
import mitris.sim.core.modeling.InPort;
import mitris.sim.core.modeling.OutPort;

/**
 *
 * @author José Luis Risco Martín
 */
public class Ef extends Coupled {

  protected InPort<Job> iIn = new InPort<Job>("iIn");
  protected OutPort<Job> oOut = new OutPort<Job>("oOut");

  public Ef(String name, double period, double observationTime) {
	  super(name);
    super.addInPort(iIn);
    super.addOutPort(oOut);
    Generator generator = new Generator("generator", period);
    addComponent(generator);
    Transducer transducer = new Transducer("transducer", observationTime);
    addComponent(transducer);
    addCoupling(this, this.iIn, transducer, transducer.iSolved);
    addCoupling(generator, generator.oOut, this, this.oOut);
    addCoupling(generator, generator.oOut, transducer, transducer.iArrived);
    addCoupling(transducer, transducer.oOut, generator, generator.iStop);
  }
}
