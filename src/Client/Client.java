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
import java.net.*;
import java.util.Date;
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

    boolean loggedIn;

    public Client(ServerSocket pushServerSocket, Socket outbound) {
        this.pushServerSocket = pushServerSocket;
        this.outbound = outbound;
    }

    //Enumerate servers and find the ones with the lowest ping;
    private int chooseServer()
    {
        long shortestPing = 999999999;
        int fastestServer = 20100;
        int i = fastestServer;
        BufferedReader reader;
        PrintWriter writer;
        try
        {
            while(true)
            {
                Socket s = new Socket(host, i + 1);

                Packet packet = new Packet(PacketTypes.proReqPacket, 0, false, null, null, null);
                ProReqData proReqData = new ProReqData();
                packet.setData(Serializer.convertToString(proReqData));

                reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));

                Date now = new Date();
                writer.println(Serializer.convertToString(packet));
                writer.flush();
                String res = reader.readLine();

                packet = (Packet) Serializer.unserializeFromString(res);
                ProResData proResData = (ProResData) Serializer.unserializeFromString(packet.getData());

                long ping = proResData.getTimestamp().getTime() - now.getTime();

                if(ping < shortestPing)
                {
                    fastestServer = i;
                }
                s.close();
                i += 20;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fastestServer;
    }
    public Client() throws IOException {
        pushServerSocket = new ServerSocket(0);
        System.out.print("Inited push server on port ");
        System.out.println(pushServerSocket.getLocalPort());
        host = "localhost";
        outbound = new Socket(host, chooseServer());

        //Set up comms

        keyboard = new BufferedReader(new InputStreamReader(System.in));
        input = new BufferedReader (new InputStreamReader(outbound.getInputStream()));
        output = new PrintWriter (outbound.getOutputStream(),true);


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
        boolean first = true;
                while(true) {
                    try {
                        Socket pushSocket = pushServerSocket.accept();
                        if(first)
                            pushSocket.setSoTimeout(1000);
                        else
                            pushSocket.setSoTimeout(0);
                        first = false;
                        pushLoop(pushSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        });

        pushThread.start();

        fileDB = new FileDB();

        mainLoop();
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
        if(resp.equals("Success"))
            loggedIn = true;
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
            int fastestPort = 0;
            InetAddress fastestHost = null;
            long shortestPing = 0;

            BufferedReader reader;
            PrintWriter writer;
            for(i = 0; i < resData.getIP().size(); i++)
            {
                InetAddress ip = (InetAddress) ipIterator.next();
                int port = (int) portIterator.next();
                System.out.println("Found file in host " + ip + " on port " + port);
                Packet pingPacket = new Packet(PacketTypes.proReqPacket, 0, false, null, null, null);
                ProReqData data = new ProReqData();
                pingPacket.setData(Serializer.serializeToString(data));

                try {
                    Socket socket = new Socket(ip, port);
                    socket.setSoTimeout(10000);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    writer.println(Serializer.serializeToString(pingPacket));
                    writer.flush();
                    Date now = new Date();
                    String res = reader.readLine();
                    Packet resPacket = (Packet) Serializer.unserializeFromString(res);
                    ProResData proResData = (ProResData)  Serializer.unserializeFromString(resPacket.getData());
                    long ping = proResData.getTimestamp().getTime() - now.getTime();
                    System.out.println("Host " + ip + " has ping " + ping);


                    if(shortestPing > ping)
                    {
                        shortestPing = ping;

                        fastestHost = ip;
                        fastestPort = port;
                    }

                }catch (IOException e)
                {
                    System.out.println("Host " + ip + " timed out;");
                }

                System.out.println("Fastest host is " + fastestHost + " on " + fastestPort + "with ping " + shortestPing);
            }
            if(i == 0)
            {
                System.out.println("No hosts found");
            }
        }
    }

    private void handleLogout() throws IOException {
        loggedIn = false;
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

            output.println(pushServerSocket.getLocalPort());
            System.out.println("Connection Established!\n"
                    + "Menu\n"
                    + "Register" + "...\n"
                    + "Login" + "...\n");
            if(loggedIn)
            {
                System.out.println("Request File...");
            }
            String s,resp;
            resp = "";
            while(!resp.equals("Saiu do sistema")){
                boolean rec = false;
                s = keyboard.readLine();
                if(s.equalsIgnoreCase("Register"))
                {
                    handleRegister();
                }
                if(s.equalsIgnoreCase("Login"))
                {
                    handleLogin();
                }

                if(s.equalsIgnoreCase("Request File") && loggedIn)
                {
                    handleRequest();
                }
                if(s.equalsIgnoreCase("Logout") && loggedIn)
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
        }if(p.getType() == PacketTypes.proReqPacket)
        {
            resp.setType(PacketTypes.proResPacket);
            ProResData data = new ProResData();
            data.setTimestamp(new Date());
            resp.setData(Serializer.serializeToString(data));
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
