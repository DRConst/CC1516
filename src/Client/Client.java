/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Commons.*;
import Server.Packet;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

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

    FileDB fileDB;

    public Client(ServerSocket pushServerSocket, Socket outbound) {
        this.pushServerSocket = pushServerSocket;
        this.outbound = outbound;
    }

    public Client() throws IOException {
        pushServerSocket = new ServerSocket(0);
        System.out.print("Inited push server on port ");
        System.out.println(pushServerSocket.getLocalPort());
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

        Thread pushThread = new Thread(() -> {
            try {
                while(true)
                {
                    Socket pushSocket = pushServerSocket.accept();
                    pushLoop(pushSocket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pushThread.start();

        fileDB = new FileDB();

        mainLoop();
    }

    public void inboundLoop() throws IOException {
        Socket server = pushServerSocket.accept();
    }

    private void handleRegister() throws IOException {
        Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
        System.out.println("Username e Password");
        String user = keyboard.readLine();
        String pass = keyboard.readLine();
        packet.setData(Serializer.convertToString(new RegisterData(pushServerSocket.getInetAddress(), pushServerSocket.getLocalPort(),user, pass )));
        output.println(Serializer.serializeToString(packet));
        String resp = input.readLine();
        System.out.println(resp);
    }

    private void handleLogin() throws IOException {
        Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
        System.out.println("Username e Password");
        uName = keyboard.readLine();
        pass = keyboard.readLine();

        //Send Login Packet
        packet.setType(PacketTypes.loginPacket);
        packet.setData(Serializer.convertToString(new LoginData(uName, pass, pushServerSocket.getInetAddress(), pushServerSocket.getLocalPort(), false )));
        output.println(Serializer.serializeToString(packet));
        String resp = input.readLine();
        System.out.println(resp);
    }

    private void handleRequest() throws IOException, UnexpectedPacketException {
        Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
        String fileName;
        fileName = keyboard.readLine();
        packet.setType(PacketTypes.conReqPacket);
        ConReqData d = new ConReqData(fileName);
        packet.setData(Serializer.convertToString(d));
        output.println(Serializer.serializeToString(packet));
        String resp = input.readLine();
        Packet r = (Packet)Serializer.unserializeFromString(resp);
        if(r.getType() != PacketTypes.conResPacket)
        {
            throw new UnexpectedPacketException("Expecting a ConResPacket, got a type " + r.getType());
        }else{
            int i;
            ConResData resData = (ConResData) Serializer.unserializeFromString(r.getData());
            Iterator ipIterator = resData.getIP().iterator();
            Iterator portIterator = resData.getPorts().iterator();
            for(i = 0; i < resData.getIP().size(); i++)
            {
                System.out.println("Found file in host " + ipIterator.next() + " on port " + portIterator.next());
            }
        }
    }

    private void handleLogout() throws IOException {
        Packet packet = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
        packet.setType(PacketTypes.loginPacket);
        packet.setData(Serializer.convertToString(new LoginData(uName, pass , true)));
        output.println(Serializer.serializeToString(packet));
        String resp = input.readLine();
        System.out.println(resp);
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
                    handleRegister();
                }
                if(s.equalsIgnoreCase("Login"))
                {
                    handleLogin();
                }

                if(s.equalsIgnoreCase("Request File"))
                {
                    handleRequest();
                }
                if(s.equalsIgnoreCase("Logout"))
                {
                    handleLogout();
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (UnexpectedPacketException e) {
            e.printStackTrace();
        }
    }

    private void pushLoop(Socket pushSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(pushSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(pushSocket.getOutputStream()));

        Packet p = (Packet) Serializer.unserializeFromString(reader.readLine());
        Packet resp = new Packet(PacketTypes.registerPacket, 0, false,null, null, null);
        if(p.getType() == PacketTypes.conReqPacket)
        {
            ConReqData data = (ConReqData) Serializer.unserializeFromString(p.getData());

            if(fileDB.fileList.contains(data.getSongName())){
                resp.setType(PacketTypes.conResPacket);
                ConResData res = new ConResData();
                res.setFound(true);
                resp.setData(Serializer.serializeToString(res));
            }
        }
        writer.println(Serializer.serializeToString(resp));
        writer.flush();
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
                    //System.out.println("Server HB successful");
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
