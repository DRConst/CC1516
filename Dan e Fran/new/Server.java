package Server;

public class Server extends Thread
{
    public static boolean runningServer = false;
    
    public static void main(String[] args) 
    {		
        Files.load();
        
        ClientMain app = new ClientMain();
        Connections conn = new Connections();
        BroadcastConnections bConns = new BroadcastConnections();

        app.start();
        conn.start();
        bConns.start();
        
        try 
        {
            app.join();
            conn.join();
            bConns.join();
        } 
        catch (InterruptedException ex) 
        { System.err.println("Error on join!"); }
        
        Files.save();
    }
}
