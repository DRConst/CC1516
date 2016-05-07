/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Commons.LoginData;
import Commons.PacketTypes;
import Commons.RegisterData;
import Commons.Serializer;
import Server.Packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Diogo
 */
public class Client {
    ServerSocket inbound;
    Socket outbound;

    BufferedReader keyboard;
    BufferedReader inboundInput, outboundInput;
    PrintWriter outboundOutput, inboundOutput;

    public Client(ServerSocket inbound, Socket outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public Client() throws IOException {
        inbound = new ServerSocket();
        inbound.bind(new InetSocketAddress(0));
        outbound = new Socket("localhost", 20123);

        //Set up comms

        keyboard = new BufferedReader(new InputStreamReader(System.in));
        inboundInput = new BufferedReader (new InputStreamReader(outbound.getInputStream()));
        outboundOutput = new PrintWriter (outbound.getOutputStream(),true);



        Thread inboundT = new Thread(() -> {
            try {
                inboundLoop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        inboundT.start();
        mainLoop();
    }

    public void inboundLoop() throws IOException {
        Socket server = inbound.accept();
    }
    public void mainLoop()
    {
        try{
            Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
            outboundOutput.println(inbound.getLocalPort());
            System.out.println("Conexão efetuada!\n"
                    + "Menu\n"
                    + "Registar" + "...\n"
                    + "Login" + "...\n");
            String s,resp;
            resp = "";
            while(!resp.equals("Saiu do sistema")){
                s = keyboard.readLine();
                System.out.println("Username e Password");
                String user = keyboard.readLine();
                String pass = keyboard.readLine();
                if(s.equals("Registar"))
                {
                    packet.setData(Serializer.convertToString(new RegisterData(outbound.getInetAddress(), outbound.getPort(),user, pass )));
                }
                if(s.equals("Login"))
                {
                    packet.setType(PacketTypes.loginPacket);
                    packet.setData(Serializer.convertToString(new LoginData(user, pass )));
                }
                outboundOutput.println(Serializer.serializeToString(packet));
                resp = inboundInput.readLine();
                System.out.println(resp);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        new Client();
    }
}
