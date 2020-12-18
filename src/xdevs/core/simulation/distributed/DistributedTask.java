/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.util.concurrent.Callable;
import xdevs.core.simulation.SimulationClock;

/**
 *
 * @author lalmendras
 */
public class DistributedTask implements Callable<String> {
    // DistributedTask attributes
    protected Node node;
    protected int command;
    protected String message;
    
    // DistributedTask constructors
    public DistributedTask(Node node, int command, String message) {
        this.node = node;
        this.command = command;
        this.message = message;        
    }
    
    // DistributedTask methods
    @Override
    public String call() {
        MessageDistributed md = new MessageDistributed(this.command, this.message);
        PingMessage pm = new PingMessage(md, this.node.host, this.node.port);
        md = pm.ping();        
        return md.getMessage();
    }
}
