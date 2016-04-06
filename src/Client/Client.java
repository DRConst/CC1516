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
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import com.sun.xml.internal.fastinfoset.sax.SystemIdResolver;

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

    public Client(ServerSocket inbound, Socket outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    public Client() throws IOException {
        inbound = new ServerSocket();
        inbound.bind(new InetSocketAddress(0));
        outbound = new Socket("localhost", 20123);
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
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader (new InputStreamReader(outbound.getInputStream()));
            PrintWriter out = new PrintWriter (outbound.getOutputStream(),true);
            out.println(inbound.getLocalPort());
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
                out.println(Serializer.serializeToString(packet));
                resp = in.readLine();
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
