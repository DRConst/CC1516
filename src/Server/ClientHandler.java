package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Commons.*;

import javax.sql.rowset.serial.SerialArray;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 *
 * @author Diogo
 */
public class ClientHandler implements Runnable{
    private Socket clientMain;
    private Socket clientPush,hbCliSocket;
    private ServerSocket hbSvrSocket;
    private BufferedReader in, hbIn;
    private PrintWriter out, hbOut;
    private Users utilizadores;
    private Login login;
    private User activeUser = null;
    private int serverID;
    private ArrayList<Integer> secondaryServerPorts;
    private ArrayList<InetAddress> secondaryServerIPs;
    private ReentrantLock secondaryServerLock;
    public ClientHandler(Socket client, Users utilizadores, Login login, int serverID, ArrayList<Integer> secondaryServerPorts, ArrayList<InetAddress> secondaryServerIPs, ReentrantLock reentrantLock) throws IOException{
        this.clientMain = client;
        this.in= new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out= new PrintWriter(client.getOutputStream(),true);
        this.utilizadores=utilizadores;
        this.login = login;
        this.serverID = serverID;
        this.secondaryServerPorts = secondaryServerPorts;
        this.secondaryServerIPs  =secondaryServerIPs;
        this.secondaryServerLock = reentrantLock;

    }
    
    public int handle() throws IOException, InterruptedException{
        int flag=1;

        Packet p = (Packet) Serializer.unserializeFromString(in.readLine());

        if(p.getType() == PacketTypes.registerPacket)
        {
            RegisterData reg = (RegisterData) Serializer.unserializeFromString(p.data);
            try {
                if(!reg.isServer()) {
                    this.login.registerUser(reg.getUserName(), reg.getPassword());

                    activeUser = login.authenticateUser(reg.getUserName(), reg.getPassword());
                    activeUser.setPort(reg.getPort());
                    activeUser.setIp(reg.getIP());


                    out.println("Success");
                }else{

                }

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UserRegisteredException e) {
                e.printStackTrace();
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            }

        }else if(p.getType() == PacketTypes.loginPacket)
        {
            LoginData reg = (LoginData) Serializer.unserializeFromString(p.data);
            try {
                //TODO: More complex info e.g. user was already logged out
                activeUser = this.login.authenticateUser(reg.getUsername(), reg.getPassword());
                activeUser.setLogged(!reg.isLogout());
                login.setLoggedIn(activeUser.getUsername(), !reg.isLogout(), serverID);
                activeUser.setIp(reg.getIP());
                activeUser.setPort(reg.getPort());
                System.out.println( "Got user on port : " + activeUser.getPort() + " from IP : " + activeUser.getIp());
                out.println("Success");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (LoginFailedException e) {
                e.printStackTrace();
                out.println("Error");
            } catch (UserNotFoundException e) {
                e.printStackTrace();
                out.println("Error");
            }
        }else if(p.getType() == PacketTypes.conReqPacket)
        {
            ConReqData data = (ConReqData)Serializer.unserializeFromString(p.data);
            Packet packet = new Packet(PacketTypes.conReqPacket, 1, false, null, null, null);
            ConReqData reqData = new ConReqData(data.getSongName());
            packet.setData(Serializer.convertToString(reqData));

            ArrayList<InetAddress> hosts = new ArrayList<>();
            ArrayList<Integer> ports = new ArrayList<>();
            for(User u : login.getUsers().values())
            {
                if(u.isLogged() && login.loggedIntoServer(u.getUsername(), serverID) && !u.getUsername().equals(activeUser.getUsername()))
                {
                    try {
                        Socket c = new Socket(u.getIp(), u.getPort());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(c.getOutputStream()));

                        writer.println(Serializer.convertToString(packet));
                        writer.flush();
                        String resp = reader.readLine();

                        Packet r = (Packet) Serializer.unserializeFromString(resp);
                        if (r.data != null) {
                            ConResData resData = (ConResData) Serializer.unserializeFromString(r.data);

                            if (resData.isFound()) {
                                hosts.add(u.getIp());
                                ports.add(u.getPort());
                            }
                        }
                    }catch (IOException e)
                    {
                        System.out.println(e.getMessage());
                    }

                }

            }

            if( serverID == 0 && (ports.size() == 0 || hosts.size()==0 ))
            {
                // Consult other servers

                BufferedReader reader;
                PrintWriter writer;
                String host = "localhost";
                Iterator portIterator = secondaryServerPorts.iterator();
                Iterator ipIterator = secondaryServerIPs.iterator();
                while(portIterator.hasNext() && ipIterator.hasNext())
                    try
                    {

                        Socket s = new Socket((InetAddress) ipIterator.next(), (Integer) portIterator.next());

                        Packet serverRequestPacket = new Packet(PacketTypes.conReqPacket, 0, false, null, null, null);
                        ConReqData requestData = new ConReqData(reqData.getSongName(), true);
                        serverRequestPacket.setData(Serializer.convertToString(requestData));

                        reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));

                        writer.println(-2);
                        writer.flush();

                        serverRequestPacket.setData(Serializer.serializeToString(requestData));
                        writer.println(Serializer.serializeToString(serverRequestPacket));
                        writer.flush();

                        String response = reader.readLine();

                        Packet serverResponsePacket = (Packet) Serializer.unserializeFromString(response);
                        ConResData resData = (ConResData) Serializer.unserializeFromString(serverResponsePacket.getData());

                        hosts.addAll(resData.getIP());
                        ports.addAll(resData.getPorts());


                        s.close();

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }

            packet.setType(PacketTypes.conResPacket);
            ConResData toRet = new ConResData();
            toRet.setIP(hosts);
            toRet.setPorts(ports);
            packet.setData(Serializer.serializeToString(toRet));
            out.println(Serializer.serializeToString(packet));
        }else if(p.getType() == PacketTypes.proReqPacket)
        {
            Packet packet = new Packet(PacketTypes.proResPacket, 1, false, null, null, null);
            Date now = new Date();
            ProResData data = new ProResData();
            data.setTimestamp(now);
            packet.setData(Serializer.serializeToString(data));
            out.println(Serializer.serializeToString(packet));
            out.flush();
        }
        return flag;
    }

    private void initHeartbeat() throws IOException {
        hbSvrSocket = new ServerSocket(0);
        int hbPort = hbSvrSocket.getLocalPort();
        hbSvrSocket.setSoTimeout(10000);
        out.println(hbPort);
        out.flush();
        hbCliSocket = hbSvrSocket.accept();
        hbCliSocket.setSoTimeout(10000);

        hbOut = new PrintWriter(new OutputStreamWriter(hbCliSocket.getOutputStream()));
        hbIn = new BufferedReader(new InputStreamReader(hbCliSocket.getInputStream()));

        hbSvrSocket.close();
    }
    public void heartbeat() throws ClientTimedOutException {

        String response;
        boolean timeout = false;
        while(!timeout)
        {
            hbOut.println("heart");
            hbOut.flush();


            try
            {
                response = hbIn.readLine();
                if(!response.equals("beat"))
                {
                    //Something went wrong, just drop the connection;
                    throw new ClientTimedOutException();
                }
            } catch (IOException e) {
                throw new ClientTimedOutException();
            }


            try {
                Thread.sleep(500); //Only ping every half second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
    
    @Override
    public void run() {
        try {  
            try {
                String port = in.readLine();

                if(port.equals("-1"))
                {


                    String register = in.readLine();

                    Packet registerPacket = (Packet) Serializer.unserializeFromString(register);
                    RegisterData data = (RegisterData) Serializer.unserializeFromString(registerPacket.getData());

                    secondaryServerLock.lock();

                    secondaryServerIPs.add(data.getIP());
                    secondaryServerPorts.add(data.getPort());

                    secondaryServerLock.unlock();
                }
                else if(port.equals("-2"))
                {
                    //Master server requesting file list

                    activeUser = new User("", "", -1);
                }
                else
                {
                        clientPush = new Socket(clientMain.getInetAddress(), new Integer(port));

                        Thread heartbeatThread = new Thread(() -> {

                            try {

                                heartbeat();
                            } catch (ClientTimedOutException e) {
                                System.out.println("Client on port " + clientMain.getLocalPort() + " timed out.");

                                if (activeUser != null) {
                                    activeUser.setLogged(false);
                                    login.setLoggedIn(activeUser.getUsername(), false, serverID);
                                }

                            }
                        });

                        try {
                            initHeartbeat();
                        } catch (IOException e) {//Failed to open ports
                            System.out.println("Error setting up heartbeat, please try again");
                        }

                        heartbeatThread.start();

                }
                while (handle() != 0) ;
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            out.println("Error");
            if(secondaryServerLock.isHeldByCurrentThread())
                secondaryServerLock.unlock();
          }
    }
    
}
