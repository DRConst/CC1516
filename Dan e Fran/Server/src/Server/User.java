package Server;

public class User 
{
    private String name;
    private String userName;
    private String pass;
    private double points;
    
    User()
    {
        name = "";
        userName = "";
        pass = "";
        points = 0;
    }
    
    User (String nx, String ux, String px, double poix)
    {
        name = nx;
        userName = ux;
        pass = px;
        points = poix;
    }
    
    public String getName() 
    { return name; }
    public String getUserName() 
    { return userName; }
    public String getPass() 
    { return pass; }
    public double getPoints()
    { return points; }

    public void setName(String name) 
    { this.name = name; }
    public void setUserName(String userName) 
    { this.userName = userName; }
    public void setPass(String pass) 
    { this.pass = pass; }
    public void setPoints(double poix)
    { this.points = poix; }
    
    public void addPoints(double p)
    { points = points + p; }
}
