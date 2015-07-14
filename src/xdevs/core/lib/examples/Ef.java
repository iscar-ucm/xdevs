package xdevs.core.lib.examples;

import xdevs.core.modeling.Coupled;
import xdevs.core.modeling.InPort;
import xdevs.core.modeling.OutPort;

/**
 *
 * @author José Luis Risco Martín
 */
public class Ef extends Coupled {

  protected InPort<Job> iStart = new InPort<>("iStart");
  protected InPort<Job> iIn = new InPort<>("iIn");
  protected OutPort<Job> oOut = new OutPort<>("oOut");
  protected OutPort<Result> oResult = new OutPort<>("oResult");

  public Ef(String name, double period, double observationTime) {
	  super(name);
    super.addInPort(iIn);
    super.addInPort(iStart);
    super.addOutPort(oOut);
    super.addOutPort(oResult);
    Generator generator = new Generator("generator", period);
    addComponent(generator);
    Transducer transducer = new Transducer("transducer", observationTime);
    addComponent(transducer);
    
    addCoupling(this.iIn, transducer.iSolved);
    addCoupling(generator.oOut, this.oOut);
    addCoupling(generator.oOut, transducer.iArrived);
    addCoupling(transducer.oOut, generator.iStop);
    addCoupling(this.iStart, generator.iStart);
    addCoupling(transducer.oResult, this.oResult);
  }
}
