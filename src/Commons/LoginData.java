package Commons;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by drcon on 06/04/2016.
 */
public class LoginData extends Data implements Serializable {
    String username;
    String password;
    InetAddress IP;
    int port;
    boolean isLogout = false;

    /*
    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }*/
    public LoginData(String username, String password, boolean isLogout) {
        this.username = username;
        this.password = password;
        this.isLogout = isLogout;
    }

    public LoginData(String username, String password, InetAddress IP, int port, boolean isLogout) {
        this.username = username;
        this.password = password;
        this.IP = IP;
        this.port = port;
        this.isLogout = isLogout;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLogout() {
        return isLogout;
    }

    public void setLogout(boolean logout) {
        isLogout = logout;
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
}
