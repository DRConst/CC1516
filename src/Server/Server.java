/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo
 */
public class Server implements Runnable {

    /**
     * @param args the command line arguments
     */
    
    private Users utilizadores;
    private Login login = null;
    Commons.Serializer serializer = new Commons.Serializer();

    
    public Server(Users utilizadores){
        this.utilizadores=utilizadores;
    }
    
    private void saveState() {
            while(true){
                    try {
                        sleep(1);
                        serializer.writeObject(login);
                        System.out.println("State saved");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("State asd");
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("State dsa");
                    }
                }
    }
    
    @Override
    public void run() {
        try {
            login = (Login) serializer.readObject("Server.Login");
            if (login == null) {
                login = new Login();
                login.setUserStorage(utilizadores);
            } 
            ServerSocket s = new ServerSocket(20123);
            Socket client;
            System.out.println("Server is operational.");
            Thread loginsaver = new Thread(new Runnable(){
                public void run(){
                    saveState();
                }
            });
            loginsaver.start();
            
            while (true) {
                client = s.accept ();
                System.out.println("Cliente ligado.");
                Thread t = new Thread(new Interface(client,utilizadores,login));
                t.start();
            }
        } catch (IOException |ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
       
    }
    
}
