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
 * 
 */
package xdevs.core.atomic.sinks;

import java.util.Collection;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;

/**
 *
 * @author José Luis Risco Martín
 */
public class Console extends Atomic {

    //private static final Logger logger = Logger.getLogger(Console.class.getName());

    public InPort<Object> iIn = new InPort<>("iIn");
    // Parameters
    protected double time;

    /**
     * Console atomic model.
     *
     * @param name Name of the atomic model
     */
    public Console(String name) {
    	super(name);
        super.addInPort(iIn);
    }
    
    @Override
    public void initialize() {
        this.time = 0.0;
    	super.passivate();
    }
    
    @Override
    public void exit() {        
    }
    
    @Override
    public void deltint() {
        super.passivate();
    }

    @Override
    public void deltext(double e) {
        time += e;
        if (iIn.isEmpty()) {
            return;
        }
        System.out.print(name);
        System.out.print("::");
        System.out.print(time);
        Collection<Object> values = iIn.getValues();
        for (Object value : values) {
            System.out.print(":");
            System.out.print(value.toString());
        }
        System.out.println("");
    }

    @Override
    public void lambda() {
    }
}
