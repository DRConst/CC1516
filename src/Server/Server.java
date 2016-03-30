/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Diogo
 */
public class Server implements Runnable {

    /**
     * @param args the command line arguments
     */
    
    private Users utilizadores;
    
    public Server(Users utilizadores){
        this.utilizadores=utilizadores;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket s = new ServerSocket(123);
            Socket client;
            System.out.println("Server is operational.");
            while (true) {
                client = s.accept ();
                System.out.println("Cliente ligado.");
                Thread t = new Thread(new Interface(client,utilizadores));
                t.start();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
       
    }
    
}
