package Commons;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * Created by drcon on 16/03/2016.
 */
public class RegisterData extends Data implements Serializable{
    boolean in;
    InetAddress IP;
    int port;
    int ID;
    String userName;
    String password;

    public RegisterData(InetAddress IP, int port, String userName, String password) {
        this.IP = IP;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public InetAddress getIP() {
        return IP;
    }

    public void setIP(InetAddress IP) {
        this.IP = IP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
