package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientMain extends Thread
{
    private boolean running;
    DatagramSocket socket = null;
    ServerHandlerMain shm = new ServerHandlerMain();
    ServerHandlerSecon shs = new ServerHandlerSecon();
    
    public void run()
    {
        running = true;
        
        do
        {
            switch(UX.initMenu())
            {
            case 1:
                if(!Server.runningServer)
                {
                    shm.start();
                    Server.runningServer = true;
                }
                else
                    UX.askGeneric("\nA server is already running!");
                break;
            case 2:
                if(!Server.runningServer)
                {
                    shs.start();
                    Server.runningServer = true;
                }
                else
                    UX.askGeneric("\nA server is already running!");
                break;
            case 3:
                running = false;
                Connections.running = false;
                UX.askGeneric("\nBye! Bye!");
                killLast();
                break;
            }
        }while(running);
    }
            
    public void killLast()
    {
        try
        {
            socket = new DatagramSocket() ;
            byte [] data = "Buh-Bye Server".getBytes() ;
            
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            
            DatagramPacket packet = new DatagramPacket( data, data.length, addr, 10000 ) ;
            
            socket.send(packet);
        }
        catch(IOException ioe)
        { System.err.println("Error while trying to kill the last connection!"); }
        finally
        {
            if(socket != null)
                socket.close();
        }
    }
}
