/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Almendras
 */
public class MessageDistributed implements Serializable{
    // Atributtes SerializedPorts
    private int command;
    private String message;
    private Collection<?> valuesPort;
    
    // Constructors SerializedPorts

    public MessageDistributed(int command, String message, Collection<?> valuesPort){
        this.command = command;
        this.message = message;
        this.valuesPort = valuesPort;
    }
    
    public MessageDistributed(int command, String message){
        this(command,message,new ArrayList<>());
    }

    public MessageDistributed(String message){
        this(Commands.NONE,message);
    }
    
    public MessageDistributed(){
        this("");
    }
    
    // Methods SerializedPorts    
    public int getCommand() {
        return command;
    }
    
    public void setCommand(int command) {    
        this.command = command;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public Collection <?> getValuesPort() {
        return valuesPort;
    }

    public void setValuesPort(Collection <?> valuesPort) {
        this.valuesPort = (Collection<?>) valuesPort;
    }

    @Override
    public String toString() {
        return "MessageDistributed{" + "command=" + command + ", message=" + message + ", ports=" + valuesPort + '}';
    }
        
}
