package Server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class PropagateData 
{
    public void registerUser(User tmp)
    {
        try
        {
            for(Socket socket : ServerHandlerMain.servers)
            {
                try
                { 
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    out.println("registerUser");
                    out.flush();
                    out.println(tmp.getName());
                    out.flush();
                    out.println(tmp.getPass());
                    out.flush();
                    out.println(tmp.getUserName());
                    out.flush();
                }
                catch(IOException ioe)
                { System.err.println("Oh Darn! Error while creating printwriter in PropagateData! :'("); }
            }
        }
        catch(NullPointerException npe)
        {}
    }
    
    public void points(String userName, double points)
    {
        for(Socket socket : ServerHandlerMain.servers)
        {
            try
            {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.println("Points");
                out.flush();
                out.println(userName);
                out.flush();
                out.println(points);
                out.flush();
            }
            catch(IOException ioe)
            { System.err.println("Oh Darn! Error while creating printwriter in points! :'("); }
        }
    }
    
    public void challenge(Challenge ch)
    {
        for(Socket socket : ServerHandlerMain.servers)
        {
            try
            {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                out.println("challenge");
                out.flush();
                out.println(ch.getName());
                out.flush();
                out.println(ch.getDT().getTimeInMillis());
                out.flush();
            }
            catch(IOException ioe)
            { System.err.println("Oh Darn! Error while creating printwriter in challenge :s !"); }
        }
    }
}
