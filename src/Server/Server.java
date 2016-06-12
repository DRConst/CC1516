/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Commons.*;

import java.io.*;

import static java.lang.Thread.sleep;

import java.lang.invoke.SerializedLambda;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
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
    int serverID;
    private ArrayList<Integer> secondaryServerPorts;
    private ArrayList<InetAddress> secondaryServerIPs;
    InetAddress addr;


    ReentrantLock secondaryServerLock;
    
    public Server(Users utilizadores, int port, int serverID){
        this.utilizadores=utilizadores;
        this.port = port;
        this.serverID = serverID;

        secondaryServerIPs = new ArrayList<>();
        secondaryServerPorts = new ArrayList<>();
        secondaryServerLock = new ReentrantLock();
    }
    private void pingHandler(Socket s) throws IOException, UnexpectedPacketException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));

        String str = reader.readLine();
        Packet p = (Packet) Serializer.unserializeFromString(str);

        if(p.type == PacketTypes.proReqPacket)
        {
            ProResData proResData = new ProResData();
            proResData.setTimestamp(new Date());
            p.setData(Serializer.serializeToString(proResData));


        }if(p.type == PacketTypes.servReqPacket)
        {
            ArrayList<InetAddress> addresses = (ArrayList<InetAddress>) secondaryServerIPs.clone();
            addresses.add(addr);
            ArrayList<Integer> ports = (ArrayList<Integer>) secondaryServerPorts.clone();
            ports.add(port);
            ServResData servResData = new ServResData(ports, addresses);
            p.setData(Serializer.serializeToString(servResData));
        }

        writer.println(Serializer.serializeToString(p));
        writer.flush();


        s.close();
    }
    private void saveState() {
            while(true){
                    try {
                        sleep(1000);
                        serializer.writeObject(login, new Integer(serverID).toString());
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
            login = (Login) serializer.readObject("Server.Login" + serverID);
            if (login == null) {
                login = new Login();
                login.setUserStorage(utilizadores);
            }
            ServerSocket s = new ServerSocket(port);
            addr = s.getInetAddress();
            Socket client;
            System.out.println("Server is operational.");


            if(port != 20100)
            {
                //Register with master server
                System.out.println("Registering with master server");
                Socket master = new Socket("localhost", 20100);


                BufferedReader reader = new BufferedReader(new InputStreamReader(master.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(master.getOutputStream()));


                writer.println("-1");
                writer.flush();

                Packet masterRegistration = new Packet(PacketTypes.registerPacket, 0, false, null, null, null);
                RegisterData registerData = new RegisterData(true, s.getInetAddress(), port);
                masterRegistration.setData(Serializer.serializeToString(registerData));
                writer.println(Serializer.serializeToString(masterRegistration));
                writer.flush();
            }




            Thread loginsaver = new Thread(() -> {
                saveState();
            });
            loginsaver.start();

            Thread ui = new Thread(() -> {
                debugUI();
            });
            ui.start();

            Thread pingThread = new Thread(() -> {
                try {
                    ServerSocket pingSS = new ServerSocket(port + 1);
                    while(true){
                        Socket s1 = pingSS.accept();
                        pingHandler(s1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnexpectedPacketException e) {
                    e.printStackTrace();
                }
            });
            pingThread.start();
            
            while (true) {
                client = s.accept ();
                System.out.println("Cliente ligado.");
                Thread t = new Thread(new ClientHandler(client,utilizadores,login, serverID, secondaryServerPorts, secondaryServerIPs, secondaryServerLock));
                t.start();
            }
        } catch (IOException |ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
       
    }
    
}
