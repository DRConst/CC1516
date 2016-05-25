package Server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Files 
{
    public static void save() 
    {
        try {saveServer("server.txt");}
        catch (IOException e)
        { 
            System.err.println ("Error saving!\nLog: " + e); 
            UX.askGeneric("Press Enter to continue.");
        }
    }
	
    public static void saveServer(String local) throws IOException
    {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(local));
        oos.writeObject(Data.getUsers());
        oos.close();
    }
	
    public static void load()
    {
        try
        { loadServer("server.txt"); }
        catch(IOException | ClassNotFoundException e)
        { 
            System.err.println("Error loading" + e); 
            UX.askGeneric("Press Enter to continue.");
        }
    }

    public static void loadServer(String local) throws IOException, ClassNotFoundException
    {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(local));
        Data.setUsers((ArrayList<User>)ois.readObject());
    }
}
