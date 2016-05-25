package Server;

import java.io.*;
import java.net.*;

public class Connections extends Thread
{
    public static int PORT = 10000;
    public static boolean running = true;

    public void run()
    {
        try(DatagramSocket mainSocket = new DatagramSocket(PORT))
        {
            while(running)
            { 
                byte[] data = new byte[65507];
                DatagramPacket packet = new DatagramPacket( data, data.length );
            
                mainSocket.receive(packet); 
                
                new ConnectionHandler(packet).start();
            }
        }		
        catch(IOException ioe)
        { 
            System.err.println("Oh Darn, something went wrong :o "
                    + "\n@ accepting new packets from mainSocket."
                    + ioe); 
        }
    }
}
