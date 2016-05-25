package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.GregorianCalendar;

public class ServerHandlerSecon extends Thread
{
    public static String serverHOST = "192.168.1.119";
    public static int outPORT = 5000;
    public static int inPORT = 4500;
    Socket socket;
    ServerSocket serverSocket;
    
    public void run()
    {
        try
        { socket = new Socket(serverHOST, outPORT); }
        catch (IOException ioe)
        { System.err.println("Oh Darn! Error while trying to connect to host! :o"); }
        
        try
        { new ServerHandlerOut(socket).start(); }
        catch (IOException ioe)
        { System.err.println("Oh Darn! Error while trying to create ServerHandlerOut()! :'("); }
        
        try
        { serverSocket = new ServerSocket(inPORT); }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error while trying to create serverSocket! :o"); }
        
        try
        { 
            Socket aux = serverSocket.accept();
            new ServerHandlerIn(aux).start(); 
            load(aux);
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error while trying to create ServerHandlerIn()! :s"); }
    }
    
    public void load(Socket socketIn)
    {
        try
        {
            BufferedReader in;
            int i;
            Data dt = new Data();
            
            in = new BufferedReader(new InputStreamReader(socketIn.getInputStream()));
            
            i = Integer.parseInt(in.readLine());
            for(int j=0; j<i; j++)
            {
                String name, userName, pass;
                double points;
                
                name = in.readLine();
                userName = in.readLine();
                pass = in.readLine();
                points = Double.parseDouble(in.readLine());
                
                User us = new User(name, userName, pass, points);
                
                dt.addUser(us);
            }
            
            i = Integer.parseInt(in.readLine());
            for(int j=0; j<i; j++)
            {
                String name;
                GregorianCalendar gc =  new GregorianCalendar();
                
                name = in.readLine();
                gc.setTimeInMillis(Long.parseLong(in.readLine()));
                
                Challenge ch = new Challenge(name, gc, 1);
                
                dt.addChallenge(ch);
            }
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error while creating printwriter in challenge :s !"); }
    }
}
