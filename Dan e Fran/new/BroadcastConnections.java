package Server;

import java.io.*;
import java.net.*;

public class BroadcastConnections extends Thread
{
    public static int PORT = 10001;
    public static boolean running = true;
    
    public void run()
    {
        try(MulticastSocket mSocket = new MulticastSocket(PORT))
        {
            mSocket.joinGroup(InetAddress.getByName("225.0.0.1"));
            while(running)
            { 
                byte[] data = new byte[65507];
                DatagramPacket packet = new DatagramPacket( data, data.length );
            
                mSocket.receive(packet); 
                
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
