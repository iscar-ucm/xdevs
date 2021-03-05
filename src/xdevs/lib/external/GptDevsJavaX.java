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
package xdevs.lib.external;

import java.util.logging.Level;

import xdevs.core.modeling.Coupled;
import xdevs.core.simulation.Coordinator;
import xdevs.core.util.DevsLogger;

/**
 *
 * @author José L. Risco-Martín
 */
public class GptDevsJavaX extends Coupled {

    public GptDevsJavaX(String name, double period, double observationTime) {
    	super(name);
        GeneratorXDevs generator = new GeneratorXDevs("generator", period);
        super.addComponent(generator);
        ProcessorDevsJava processorDevsJava = new ProcessorDevsJava("processor", 3*period);
        AtomicDevsJava processor = new AtomicDevsJava(processorDevsJava);
        super.addComponent(processor);
        TransducerXDevs transducer = new TransducerXDevs("transducer", observationTime);
        super.addComponent(transducer);
        
        super.addCoupling(generator.oOut, processor.getInPort("iIn"));
        super.addCoupling(generator.oOut, transducer.iArrived);
        super.addCoupling(processor.getOutPort("oOut"), transducer.iSolved);
        super.addCoupling(transducer.oOut, generator.iStop);
    }

    public static void main(String args[]) {
        DevsLogger.setup(Level.INFO);
        GptDevsJavaX gpt = new GptDevsJavaX("gpt", 1, 100);
        //CoordinatorParallel coordinator = new CoordinatorParallel(gpt);
        Coordinator coordinator = new Coordinator(gpt);
        //RTCentralCoordinator coordinator = new RTCentralCoordinator(gpt);
        coordinator.initialize();
        coordinator.simulate(Long.MAX_VALUE);
        coordinator.exit();
    }

}
