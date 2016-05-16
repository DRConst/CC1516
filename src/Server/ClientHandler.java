package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Commons.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
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
    
    public ClientHandler(Socket client, Users utilizadores, Login login) throws IOException{
        this.clientMain = client;
        this.in= new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out= new PrintWriter(client.getOutputStream(),true);
        this.utilizadores=utilizadores;
        this.login = login;

    }
    
    public int handle() throws IOException, InterruptedException{
        int flag=1;

        Packet p = (Packet) Serializer.unserializeFromString(in.readLine());

        if(p.getType() == PacketTypes.registerPacket)
        {
            RegisterData reg = (RegisterData) Serializer.unserializeFromString(p.data);
            try {
                this.login.registerUser(reg.getUserName(), reg.getPassword());

                activeUser = login.authenticateUser(reg.getUserName(), reg.getPassword());
                activeUser.setPort(reg.getPort());
                activeUser.setIp(reg.getIP());


                out.println("Success");

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
                login.setLoggedIn(activeUser.getUsername(), !reg.isLogout());
                out.println("Success");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (LoginFailedException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            }
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
    @SuppressWarnings("empty-statement")
    public void run() {
        try {  
            try {
                String port = in.readLine();
                clientPush = new Socket(clientMain.getInetAddress(), new Integer(port));

                Thread heartbeatThread = new Thread(() -> {

                    try
                    {

                        heartbeat();
                    } catch (ClientTimedOutException e) {
                        System.out.println("Client on port " + clientMain.getLocalPort() + " timed out.");

                        if(activeUser != null)
                        {
                            activeUser.setLogged(false);
                            login.setLoggedIn(activeUser.getUsername(), false);
                        }

                    }
                });

                try {
                    initHeartbeat();
                } catch (IOException e) {//Failed to open ports
                    System.out.println("Error setting up heartbeat, please try again");
                }

                heartbeatThread.start();


                while(handle()!=0);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
          }
    }
    
}
