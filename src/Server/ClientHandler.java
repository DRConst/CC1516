package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Commons.LoginData;
import Commons.PacketTypes;
import Commons.RegisterData;
import Commons.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private Socket clientPush;
    private BufferedReader in;
    private PrintWriter out;
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

                User usr = login.authenticateUser(reg.getUserName(), reg.getPassword());
                usr.setPort(reg.getPort());
                usr.setIp(reg.getIP());
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
                User u = this.login.authenticateUser(reg.getUsername(), reg.getPassword());
                u.setLogged(true);
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
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        try {  
            try {
                String port = in.readLine();
                clientPush = new Socket(clientMain.getInetAddress(), new Integer(port));
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
