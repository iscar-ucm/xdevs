/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Almendras
 */
public class PingMessage {

    // PingMessage constants
    public static final String DEFAULT_DESTINATIONHOST = "127.0.0.1";
    public static final int DEFAULT_DESTINATIONPORT = 5000; 
    
    // PingMessage attributes    
    private MessageDistributed message;    
    private String destinationHost;
    private int destinationPort;
    
    // PingMessage Constructors
    public PingMessage(MessageDistributed message, String destinationHost, int destinationPort){
        this.message = message;
        this.destinationHost = destinationHost;
        this.destinationPort = destinationPort;
    }
    
    public PingMessage(MessageDistributed message){
        this(message, DEFAULT_DESTINATIONHOST, DEFAULT_DESTINATIONPORT);
    }
    
    // PingMessage methods
    public MessageDistributed ping() {
        MessageDistributed response = null;
        try {
            Socket sc = new Socket(this.destinationHost, this.destinationPort);            
            ObjectOutputStream out = new ObjectOutputStream(sc.getOutputStream());
            out.writeObject(this.message);
            ObjectInputStream in =  new ObjectInputStream (sc.getInputStream());            
            response = (MessageDistributed) in.readObject();
            in.close();
            out.close();
            sc.close();
        } catch (Exception e) {
            System.out.println("Error connecting to " + this.destinationHost + ":" + this.destinationPort + "... (command: " + message.getCommand() + "-" + message.getMessage() +  ")");
            e.printStackTrace();
        } 
        return response;
    }
}
