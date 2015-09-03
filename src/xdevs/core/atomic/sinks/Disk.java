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
package xdevs.core.atomic.sinks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.Logger;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.InPort;

/**
 *
 * @author jlrisco
 */
public class Disk extends Atomic {

    private static final Logger logger = Logger.getLogger(Disk.class.getName());

    public InPort<Object> iIn = new InPort<>("iIn");
    // Parameters
    protected Writer file;
    protected double time;

    /**
     * Disk atomic model.
     *
     * @param fileName name of the file (complete path)
     */
    public Disk(String name, String fileName) {
    	super(name);
        super.addInPort(iIn);
        try {
            file = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            logger.severe(e.getLocalizedMessage());
        }
    }
    
    public void initialize() {
        this.time = 0.0;
        super.passivate();    	
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
        StringBuilder line = new StringBuilder();
        line.append(time);
        Collection<Object> values = iIn.getValues();
        for (Object value : values) {
            line.append("\t").append(value.toString());
        }
        try {
            file.write(line.toString() + "\n");
            file.flush();
        } catch (IOException ee) {
            logger.severe(ee.getLocalizedMessage());
        }
    }

    @Override
    public void lambda() {
    }

    @Override
    public void finalize() throws Throwable {
        try {
            file.flush();
            file.close();
        } catch (IOException ee) {
            logger.severe(ee.getLocalizedMessage());
        } finally {
            super.finalize();
        }
    }
}
