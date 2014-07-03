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
public class Console extends Atomic {

    private static final Logger logger = Logger.getLogger(Console.class.getName());

    public Port<Object> iIn = new Port<>();
    // Parameters
    protected double time;

    /**
     * Console atomic model.
     *
     */
    public Console() {
        super.addInPort(iIn);
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
        System.out.print(time);
        Collection<Object> values = iIn.getValues();
        for (Object value : values) {
            System.out.print("\t");
            System.out.print(value.toString());
        }
        System.out.println("");
    }

    @Override
    public void lambda() {
    }
}
