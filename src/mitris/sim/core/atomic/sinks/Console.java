package mitris.sim.core.atomic.sinks;

import java.util.Collection;

import mitris.sim.core.modeling.Atomic;
import mitris.sim.core.modeling.InPort;

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
     */
    public Console(String name) {
    	super(name);
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
