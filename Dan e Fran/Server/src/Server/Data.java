package Server;

import java.util.ArrayList;

public class Data 
{
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Challenge> chal = new ArrayList<>();
    
    public static ArrayList<User> getUsers()
    { return users; }
    public static ArrayList<Challenge> getChallenges()
    { return chal; }
    
    public static void setUsers(ArrayList<User> usx)
    { users = usx; }
    public static void setChallenges(ArrayList<Challenge> chx)
    { chal = chx; }
    
    public void addUser(User us)
    { users.add(us); }
    
    public void addChallenge(Challenge ch)
    { chal.add(ch); }
    
    public boolean checkUser(String userName, String pass)
    {
        for(User us : users)
        {
            if(us.getUserName().equals(userName) && us.getPass().equals(pass))
            { return true; }
        }
        return false;
    }
    
    public boolean userExists(String userName)
    {
        for(User us : users)
        {
            if(us.getUserName().equals(userName) )
            { return true; }
        }
        return false;
    }
    
    public boolean challengeExists(String chName)
    {
        for(Challenge ch : chal)
        {
            if(ch.getName().equals(chName) )
            { return true; }
        }
        return false;
    }
    
    public User getUser(String userName)
    {
        User send = new User();
        for(User us : users)
        {
            if(us.getUserName().equals(userName))
                send = us;
        }
        return send;
    }
    
    public User getUserByName(String name)
    {
        User send = new User();
        for(User us : users)
        {
            if(us.getName().equals(name))
                send = us;
        }
        return send;
    }
    
    public Challenge getChallenge(String name)
    {
        Challenge send = new Challenge();
        for(Challenge ch : chal)
        {
            if(ch.getName().equals(name))
                send = ch;
        }
        return send;
    }
    
    public void addPoints(String u, int p)
    {
        int points = 60 - p;
        getUser(u).addPoints(points);
    }
    
    public void removeChallente(Challenge cha)
    { chal.remove(cha); }
}
