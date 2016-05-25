package Server;

import java.util.GregorianCalendar;

public class Challenge 
{
    private int challenge;
    private String name;
    private GregorianCalendar dt;
    
    Challenge()
    {
        name = "";
        dt = new GregorianCalendar();
    }
    
    Challenge(String nx, GregorianCalendar dtx, int challengeX)
    {
        name = nx;
        dt = dtx;
        challenge = challengeX;
    }
    
    public String getName()
    { return name; }
    public GregorianCalendar getDT()
    { return dt; }    
}