/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xdevs.core.simulation.distributed;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Almendras
 */
public class DistributedDaemon {
    // DistributedDaemon attributes 
    private int port;
    private SimulatorDistributed sd;
    
    // DistributedDaemon constructors
    public DistributedDaemon(int port, SimulatorDistributed sd){
        this.port = port;
        this.sd = sd;
    }
    
    public void start() {
        //ArrayBlockingQueue<Socket> queue = new ArrayBlockingQueue<>(20);
        BlockingDeque<Socket> queue = new LinkedBlockingDeque<>();

        Thread producer = new Thread(() -> {
            //System.out.println("Producer starting...");
            try {
                ServerSocketChannel ssc = ServerSocketChannel.open();
                ssc.socket().bind(new InetSocketAddress(port));
                ssc.configureBlocking(false);

                while (!sd.isGetOut()) {

                    SocketChannel sc = ssc.accept();
                    if (sc != null) {
                        queue.put(sc.socket());
                    }
                }
                ssc.close();
                //System.out.println("Producer stopping...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                //System.out.println("Consumer starting...");

                while(!sd.isGetOut()) {
                    Socket socket = queue.poll(100, TimeUnit.MILLISECONDS);
                    if(socket!=null) {
                        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                        MessageDistributed msg = (MessageDistributed) in.readObject();
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(sd.interpreter(msg));
                        in.close();
                        out.close();
                        socket.close();
                    }
                }
                //System.out.println("Consumer stopping...");
            } catch (InterruptedException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        producer.start();
        consumer.start();
    }    
}
