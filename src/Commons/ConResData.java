package Commons;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by drcon on 16/03/2016.
 */
public class ConResData extends Data implements Serializable{
    ArrayList<InetAddress> IP;
    ArrayList<Integer> ports;
    int port, ID, numHosts;
    boolean found;

    public ArrayList<InetAddress> getIP() {
        return IP;
    }

    public void setIP(ArrayList<InetAddress> IP) {
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

    public int getNumHosts() {
        return numHosts;
    }

    public void setNumHosts(int numHosts) {
        this.numHosts = numHosts;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public ArrayList<Integer> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<Integer> ports) {
        this.ports = ports;
    }
}
