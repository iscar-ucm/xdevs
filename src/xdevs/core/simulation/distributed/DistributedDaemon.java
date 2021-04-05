/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Almendras
 */
public class DistributedDaemon implements Runnable {
    // DistributedDaemon attributes 
    private int port;
    private SimulatorDistributed sd;
    
    // DistributedDaemon constructors
    public DistributedDaemon(int port, SimulatorDistributed sd){
        this.port = port;
        this.sd = sd;
    }
    
    //DistributedDaemon methods
    @Override
    public void run() {			
        try {
            ServerSocket serverSocket = new ServerSocket(this.port);            
            while (!sd.isGetOut()) {
                Socket socket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());                
                MessageDistributed msg = (MessageDistributed) in.readObject();
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(sd.interpreter(msg));                
                in.close();
                out.close();
                socket.close();                
            }
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }    
}
