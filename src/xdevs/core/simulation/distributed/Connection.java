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
public class Connection {
    // Connection attributes
    protected String from;    
    protected String to;
    protected Node fromNode;
    protected Node toNode;
    
    // Connection Constructors
    public Connection(String from, Node fromNode, String to, Node toNode){
        this.from = from;
        this.fromNode = fromNode;
        this.to = to;
        this.toNode = toNode;
    }
    
    // Connection methods
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public void setToNode(Node toNode) {
        this.toNode = toNode;
    }

    @Override
    public String toString() {
        return this.from+" - "+this.fromNode+" | "+this.to+" - "+this.toNode;
    }
    
    
}
