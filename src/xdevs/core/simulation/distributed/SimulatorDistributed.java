/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import xdevs.core.modeling.Atomic;
import xdevs.core.modeling.Coupling;
import xdevs.core.modeling.distributed.CoupledDistributed;
import xdevs.core.simulation.SimulationClock;
import xdevs.core.simulation.Simulator;

/**
 *
 * @author Almendras
 */
public class SimulatorDistributed extends Simulator {
    private static final Logger LOGGER = Logger.getLogger(SimulatorDistributed.class.getName());

    protected CoupledDistributed parent;
    protected boolean getOut = false;

    // SimulatorDistributed Constructors
    public SimulatorDistributed(CoupledDistributed parent, String name) {
        super(new SimulationClock(), (Atomic) parent.getComponentByName(name));
        this.parent = parent;
        this.model = (Atomic) parent.getComponentByName(name);
        System.out.println("I am: " + model.getName());
        System.out.println("Parent: " + parent.getName());
        this.run();
    }

    public boolean isGetOut() {
        return getOut;
    }

    public void propagateOutput() {
        MessageDistributed md;
        String nameModel = this.model.getName();
        for (Coupling c : parent.getIC()) {
            if (c.getPortFrom().getParent().getName().equals(nameModel) && c.getPortFrom().getValues().size() > 0) {
                md = new MessageDistributed(Commands.PROPAGATE_OUTPUT_N2N, c.getPortTo().getName(), c.getPortFrom().getValues());
//                System.out.println(System.currentTimeMillis() + ", " + nameModel +  ": Trying to connect to " + c.getPortTo().getParent().getName() + " (propagation: " + nameModel + "." + c.getPortFrom().getName() + " -> " + c.getPortTo().getParent().getName() + "." + c.getPortTo().getName() + ")" + "[c:" + String.valueOf(clock.getTime()) + "]");
//                System.out.println("Ping destination: " + parent.getHost(c.getPortTo().getParent().getName()) + ":" + parent.getAuxPort(c.getPortTo().getParent().getName()));
//                System.out.println("Len: " + c.getPortFrom().getValues().size());
                PingMessage pm = new PingMessage(md, parent.getHost(c.getPortTo().getParent().getName()), parent.getAuxPort(c.getPortTo().getParent().getName()));
                pm.ping();
            }
        }
    }

    public void propagateOutputN2N(Collection valuesPort, String portName) {
        this.model.getInPort(portName).addValues(valuesPort);
    }

    public MessageDistributed interpreter(MessageDistributed md) {
        MessageDistributed response = null;
        Date date = new Date();
        DateFormat now = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.");
        try {
            switch (md.getCommand()) {
                case Commands.NONE:
                    response = new MessageDistributed("NONE");
                    break;
                case Commands.INITIALIZE:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.initialize();
                    response = new MessageDistributed("INITIALIZE: OK At " + now.format(date));
                    break;
                case Commands.TA:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    response = new MessageDistributed(String.valueOf(this.getTN()));
                    break;
                case Commands.LAMBDA:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.lambda();
                    response = new MessageDistributed("LAMBDA: OK At " + now.format(date));
                    break;
                case Commands.PROPAGATE_OUTPUT:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.propagateOutput();
                    response = new MessageDistributed("PROPAGATE_OUTPUT: OK At " + now.format(date));
                    break;
                case Commands.PROPAGATE_OUTPUT_N2N:
                    this.propagateOutputN2N(md.getValuesPort(), md.getMessage());
                    response = new MessageDistributed("PROPAGATE_OUTPUT_N2N: OK At " + now.format(date));
                    break;
                case Commands.DELTFCN:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.deltfcn();
                    response = new MessageDistributed("DELTFCN: OK At " + now.format(date));
                    break;
                case Commands.CLEAR:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.clear();
                    response = new MessageDistributed("CLEAR: OK At " + now.format(date));
                    break;
                case Commands.EXIT:
                    clock.setTime(Double.parseDouble(md.getMessage()));
                    this.exit();
                    response = new MessageDistributed("EXIT: OK At " + now.format(date));
                    this.getOut = true;
                    break;
//                case Commands.EXIT_AUX:
//                    clock.setTime(Double.parseDouble(md.getMessage()));
//                    response = new MessageDistributed("EXIT_AUX: OK At " + now.format(date));
//                    break;
                default:
                    response = new MessageDistributed("BAD_COMMAND");
                    break;
            }

        } catch (NumberFormatException ex) {
            LOGGER.severe(ex.getLocalizedMessage());
        }
        return response;
    }

    public void run() {
        // For to attend the communication with the coordinator
        DistributedDaemon mainDaemon = new DistributedDaemon(parent.getMainPort(model.getName()), this);
        mainDaemon.start();
        // For to attend the communication with the workers (At this case to propagate)
        DistributedDaemon auxDaemon = new DistributedDaemon(parent.getAuxPort(model.getName()), this);
        auxDaemon.start();
    }

}
