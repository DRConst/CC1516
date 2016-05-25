package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.GregorianCalendar;

public class ServerHandlerIn extends Thread
{
    private final Socket socket;
    private final BufferedReader in;
    
    private final Data data = new Data();
    
    public ServerHandlerIn(Socket socketX) throws IOException 
    {
        socket = socketX;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void run()
    {
        System.out.println("Incoming connection established on socket: " + socket);
        try
        {
            String request;
            request = clientRequest();
            while(!request.equals("quit")) 
            {
                switch (request) 
                {
                    case "registerUser":
                        registerUser();
                        request = clientRequest();
                        break;
                    case "Points":
                        points();
                        request = clientRequest();
                        break;
                    case "challenge":
                        challenge();
                        request = clientRequest();
                        break;
                    default:
                        System.err.println("Bad request!\nQuitting!");
                        request = "quit";
                }
            }
            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        }
        catch(IOException i)
        { System.err.println("Oh Darn! Connection lost on socket: " + socket); }
    }
    
    public String clientRequest()
    {
        String s = "";
        try { s = in.readLine(); }
        catch (IOException io)
        { System.err.println("Oh Darn! Error while reading from client!"); }
        return s;
    }
    
    public void registerUser()
    {
        String name = "", userName = "", password = "";
        try
        {
            name = in.readLine();
            password = in.readLine();
            userName = in.readLine();
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error reading line in register user! :o"); }
        
        User tmp;
        if(!name.equals(""))
        {
            tmp = new User(name, userName, password, 0);
        
            if(!data.userExists(userName))
                data.addUser(tmp);
            
            if(ServerHandlerMain.isMain)
            { 
                PropagateData pd = new PropagateData(); 
                pd.registerUser(tmp);
            }
        }
    }
    
    public void points()
    {
        String userName = "";
        double points = 0;
        
        try
        {
            userName = in.readLine();
            points = Double.parseDouble(in.readLine());
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error reading line in points()! :o"); }
        
        if(!userName.equals(""))
        {
            data.getUser(userName).setPoints(points);
            
            if(ServerHandlerMain.isMain)
            { 
                PropagateData pd = new PropagateData(); 
                pd.points(userName, points);
            }
        }
    }
    
    public void challenge()
    {
        String name = "";
        GregorianCalendar gc = new GregorianCalendar();
        
        try
        {
            name = in.readLine();
            gc.setTimeInMillis(Long.parseLong(in.readLine()));
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Error reading line in challenge :o!"); }
        
        if(!name.equals(""))
        {
            Challenge ch = new Challenge(name, gc, 1);
            data.addChallenge(ch);
            
            if(ServerHandlerMain.isMain)
            {
                PropagateData pd = new PropagateData(); 
                pd.challenge(ch);
            }
        }
    }
}
