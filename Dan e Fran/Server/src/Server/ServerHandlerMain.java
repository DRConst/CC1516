package Server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerHandlerMain extends Thread
{   
    public static int inPORT = 5000;
    public static int outPORT = 4500;
    public static boolean running = true;
    public static boolean isMain = false;
    public static ArrayList<Socket> servers = new ArrayList();
    
    public void run()
    {
        isMain = true;
        
        Files.load();
        
        try 
        {
            ServerSocket serverSocket = new ServerSocket(inPORT);
            while(running)
            { 
                Socket socks = serverSocket.accept();
                new ServerHandlerIn(socks).start();
                
                System.out.println("HandlerMain socks: " + socks);
                
                InetAddress ia = socks.getInetAddress();
                
                Socket soc = new Socket(ia,outPORT);
                
                new ServerHandlerOut(soc).start();
                
                System.out.println("HandlerMain soc: " + soc);
                
                servers.add(soc);
                
                load(soc);
            }
        } 
        catch (IOException ex) 
        { System.err.println("Oh Darn! Can't create server socket!! :'("); }
    }
    
    public void load(Socket socketOut)
    {
        Data dt = new Data();
        
        try
        {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socketOut.getOutputStream()));
            out.println(dt.getUsers().size());
            out.flush();
            for(User us : dt.getUsers())
            {
                out.println(us.getName());
                out.flush();
                out.println(us.getUserName());
                out.flush();
                out.println(us.getPass());
                out.flush();
                out.println(us.getPoints());
                out.flush();
            }
            
            out.println(dt.getChallenges().size());
            out.flush();
            for(Challenge ch : dt.getChallenges())
            {
                out.println(ch.getName());
                out.flush();
                out.println(ch.getDT().getTimeInMillis());
                out.flush();
            }
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error while creating printwriter in load :s !"); }
    }
}
