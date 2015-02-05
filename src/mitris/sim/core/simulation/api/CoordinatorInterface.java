package mitris.sim.core.simulation.api;

import java.util.Collection;
import mitris.sim.core.modeling.InPort;

/**
 *
 * @author José Luis Risco Martín
 */
public interface CoordinatorInterface extends SimulatorInterface {

    public Collection<SimulatorInterface> getSimulators();

    public void propagateOutput();

    public void propagateInput();

    /**
     * Injects a value into the port "port", calling the transition function.
     * @param e elapsed time
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(double e, InPort port, Collection<Object> values);

    /**
     * @see mitris.sim.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(0.0, InPort, Collection)
     * @param port input port to inject the set of values
     * @param values set of values to inject
     */
    public void simInject(InPort port, Collection<Object> values);

    /**
     * Injects a single value in the given input port with elapsed time e.
     * @see mitris.sim.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(double, InPort, Collection)
     * @param e
     * @param port
     * @param value
     */
    public void simInject(double e, InPort port, Object value);

    /**
     * Injects a single value in the given input port with elapsed time e equal to 0.
     * @see mitris.sim.core.simulation.api.CoordinatorInterface#simInject(double, mitris.sim.core.modeling.InPort, java.util.Collection) simInject(0.0, InPort, Collection)
     * @param port
     * @param value
     */
    public void simInject(InPort port, Object value);

    public void simulate(long numIterations);

    public void simulate(double timeInterval);
}
