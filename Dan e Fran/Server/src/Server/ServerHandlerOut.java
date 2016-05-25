package Server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerHandlerOut extends Thread
{
    private final Socket socket;
    private final PrintWriter out;
    public static AtomicInteger controlAI = new AtomicInteger();
    private static String running;
    
    public ServerHandlerOut(Socket socketX) throws IOException
    {
        socket = socketX;
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        running = "";
        controlAI.set(0);
    }
    
    public void run()
    {
        System.out.println("Outgoing connection established on socket: " + socket);
        
        while(!running.equals("quit"))
        {
            synchronized(controlAI)
            {
                if(controlAI.get() == 0)
                {
                    try
                    { controlAI.wait(); }
                    catch (InterruptedException ie)
                    { System.err.println("Oh Darn! Error while waiting on seconOPT! :'("); }
                }
                else
                    cases(controlAI.get());
            }      
        }
    }
    
    public void cases(int option)
    {
        switch(option)
        {
            case 1:
                registerUser();
                setseconOPTWait();
                break;
            case 2:
                points();
                setseconOPTWait();
                break;
            case 3:
                challenge();
                setseconOPTWait();
                break;
            default:
                System.err.println("Bad request!\nQuitting!");
                running = "quit";
        }
    }
    
    public void registerUser()
    {
        out.println("registerUser");
        out.flush();
        out.println(ConnectionHandler.staticUser.getName());
        out.flush();
        out.println(ConnectionHandler.staticUser.getPass());
        out.flush();
        out.println(ConnectionHandler.staticUser.getUserName());
        out.flush();
    }
    
    public void points()
    {
        out.println("Points");
        out.flush();
        out.println(ConnectionHandler.staticUser.getUserName());
        out.flush();
        out.println(ConnectionHandler.staticUser.getPoints());
        out.flush();
    }
    
    public void challenge()
    {
        out.println("challenge");
        out.flush();
        out.println(ConnectionHandler.staticChallenge.getName());
        out.flush();
        out.println(ConnectionHandler.staticChallenge.getDT().getTimeInMillis());
        out.flush();
    }
    
    public void setseconOPTWait()
    {
        synchronized(controlAI)
        { controlAI.set(0); }
    }
}
