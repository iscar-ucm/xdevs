/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mitris.sim.core.atomic.sinks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.logging.Logger;
import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.Port;

/**
 *
 * @author jlrisco
 */
public class Disk extends Atomic {

    private static final Logger logger = Logger.getLogger(Disk.class.getName());

    public Port<Object> iIn = new Port<>();
    // Parameters
    protected Writer file;
    protected double time;

    /**
     * Disk atomic model.
     *
     * @param fileName name of the file (complete path)
     */
    public Disk(String fileName) {
        super.addInPort(iIn);
        try {
            file = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            logger.severe(e.getLocalizedMessage());
        }
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
