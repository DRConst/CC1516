/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Commons.PacketTypes;
import Commons.ProResData;
import Commons.Serializer;
import Commons.UnexpectedPacketException;

import java.io.*;

import static java.lang.Thread.sleep;

import java.lang.invoke.SerializedLambda;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
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
    int port;
    
    public Server(Users utilizadores, int port){
        this.utilizadores=utilizadores;
        this.port = port;
    }
    private void pingHandler(Socket s) throws IOException, UnexpectedPacketException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));

        String str = reader.readLine();
        Packet p = (Packet) Serializer.unserializeFromString(str);

        if(p.type != PacketTypes.proReqPacket)
            throw new UnexpectedPacketException("Expecting Probe Request Packet");

        ProResData proResData = new ProResData();
        proResData.setTimestamp(new Date());
        p.setData(Serializer.serializeToString(proResData));
        writer.println(Serializer.serializeToString(p));
        writer.flush();
        s.close();
    }
    private void saveState() {
            while(true){
                    try {
                        sleep(1000);
                        serializer.writeObject(login);
                        //System.out.println("State saved");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("State asd");
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("State dsa");
                    }
                }
    }

    private void debugUI()
    {
        BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
        while(true)
        {
            System.out.println("Command:");
            try {
                String response = in.readLine();

                if(response.equals("users"))
                {
                    HashMap<String,Boolean> log = login.getLoggedIn();
                    if(log != null)
                        System.out.println(log.toString());
                    else
                        System.out.println("No users logged in");
                }
            } catch (IOException e) {
                e.printStackTrace();
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
            ServerSocket s = new ServerSocket(port);
            Socket client;
            System.out.println("Server is operational.");
            Thread loginsaver = new Thread(new Runnable(){
                public void run(){
                    saveState();
                }
            });
            loginsaver.start();

            Thread ui = new Thread(new Runnable(){
                public void run(){
                    debugUI();
                }
            });
            ui.start();

            Thread pingThread = new Thread(new Runnable(){
                public void run(){
                    try {
                        ServerSocket pingSS = new ServerSocket(port + 1);
                        while(true){
                            Socket s = pingSS.accept();
                            pingHandler(s);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UnexpectedPacketException e) {
                        e.printStackTrace();
                    }
                }
            });
            pingThread.start();
            
            while (true) {
                client = s.accept ();
                System.out.println("Cliente ligado.");
                Thread t = new Thread(new ClientHandler(client,utilizadores,login));
                t.start();
            }
        } catch (IOException |ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
       
    }
    
}
