/*
 * Copyright (C) 2014-2015 José Luis Risco Martín <jlrisco@ucm.es> and 
 * Saurabh Mittal <smittal@duniptech.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, see
 * http://www.gnu.org/licenses/
 *
 * Contributors:
 *  - José Luis Risco Martín
 */
package xdevs.core.test.efp;

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
