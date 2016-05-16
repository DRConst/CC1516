/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Commons.*;
import Server.Packet;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Diogo
 */
public class Client {
    ServerSocket pushServerSocket;
    Socket outbound, hbSocket;

    BufferedReader keyboard;
    BufferedReader input, pushInput, hbIn;
    PrintWriter output, pushOutput, hbOut;

    String host;

    String uName, pass;

    public Client(ServerSocket pushServerSocket, Socket outbound) {
        this.pushServerSocket = pushServerSocket;
        this.outbound = outbound;
    }

    public Client() throws IOException {
        pushServerSocket = new ServerSocket();
        pushServerSocket.bind(new InetSocketAddress(0));
        host = "localhost";
        outbound = new Socket(host, 20123);

        //Set up comms

        keyboard = new BufferedReader(new InputStreamReader(System.in));
        input = new BufferedReader (new InputStreamReader(outbound.getInputStream()));
        output = new PrintWriter (outbound.getOutputStream(),true);



        Thread inboundT = new Thread(() -> {
            try {
                inboundLoop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        inboundT.start();

        Thread hbThread = new Thread(() -> {
            try {
                initHeartbeat();
                heartbeat();
            } catch (ServerUnreachableException e) {
                e.printStackTrace();
            }
        });

        hbThread.start();
        mainLoop();
    }

    public void inboundLoop() throws IOException {
        Socket server = pushServerSocket.accept();
    }
    public void mainLoop()
    {
        try{

            Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
            output.println(pushServerSocket.getLocalPort());
            System.out.println("Conexão efetuada!\n"
                    + "Menu\n"
                    + "Registar" + "...\n"
                    + "Login" + "...\n");
            String s,resp;
            resp = "";
            while(!resp.equals("Saiu do sistema")){
                boolean rec = false;
                s = keyboard.readLine();
                if(s.equalsIgnoreCase("Registar"))
                {

                    System.out.println("Username e Password");
                    String user = keyboard.readLine();
                    String pass = keyboard.readLine();
                    packet.setData(Serializer.convertToString(new RegisterData(outbound.getInetAddress(), outbound.getPort(),user, pass )));
                    rec = true;

                }
                if(s.equalsIgnoreCase("Login"))
                {

                    System.out.println("Username e Password");
                    uName = keyboard.readLine();
                    pass = keyboard.readLine();

                    //Send Login Packet
                    packet.setType(PacketTypes.loginPacket);
                    packet.setData(Serializer.convertToString(new LoginData(uName, pass, outbound.getInetAddress(), outbound.getPort(), false )));
                    rec = true;
                }

                if(s.equalsIgnoreCase("Logout"))
                {
                    packet.setType(PacketTypes.loginPacket);
                    packet.setData(Serializer.convertToString(new LoginData(uName, pass , true)));
                    rec = true;
                }
                if(rec)
                {
                    output.println(Serializer.serializeToString(packet));
                    resp = input.readLine();
                    System.out.println(resp);
                }

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    private void heartbeat() throws ServerUnreachableException {
        String response;
        boolean timeout = false;
        while(!timeout)
        {

            try
            {
                response = hbIn.readLine();
                if(!response.equals("heart"))
                {
                    System.out.println("Server HB Corrupted : " + response);
                    throw new ServerUnreachableException();
                }else{
                    hbOut.println("beat");
                    hbOut.flush();
                    System.out.println("Server HB successful");
                }
            } catch (IOException e) {
                throw new ServerUnreachableException();
            }
        }
    }

    private void initHeartbeat()
    {
        int port = -1;
        try {
            String portnum = input.readLine();
            System.out.println("Got port num : " + portnum);
            port = new Integer(portnum);
            hbSocket = new Socket(host, port);
            hbOut = new PrintWriter(new OutputStreamWriter(hbSocket.getOutputStream()));
            hbIn = new BufferedReader(new InputStreamReader(hbSocket.getInputStream()));
            hbSocket.setSoTimeout(10000);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        new Client();
    }
}
