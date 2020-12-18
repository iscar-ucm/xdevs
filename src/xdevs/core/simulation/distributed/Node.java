/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

/**
 *
 * @author Almendras
 */
public class Node {
    // Node attributes
    protected String name;    
    protected String host;
    protected int port;
    protected int auxPort;
    
    // Node Constructors
    public Node(String name, String host, int port, int auxPort){
        this.name = name;
        this.host = host;
        this.port = port;
        this.auxPort = auxPort;
    }
        
    public Node(String name, String className, String host, int port){
        this( name, host, port, port * 1000);
    }
    
    // Node methods

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getAuxPort() {
        return auxPort;
    }

    public void setAuxPort(int auxPort) {
        this.auxPort = auxPort;
    }
    
    @Override
    public String toString() {
        return "(" + this.name+" - "+this.host+" - "+this.port+" - "+this.auxPort + ")";
    }
    
    
}
