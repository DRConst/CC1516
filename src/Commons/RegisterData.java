package Commons;

import java.net.Inet4Address;

/**
 * Created by drcon on 16/03/2016.
 */
public class RegisterData extends Data {
    boolean in;
    String IP;
    int port;
    int ID;

    public boolean isIn() {
        return in;
    }

    public void setIn(boolean in) {
        this.in = in;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
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
}
