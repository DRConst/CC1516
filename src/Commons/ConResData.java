package Commons;

/**
 * Created by drcon on 16/03/2016.
 */
public class ConResData extends Data {
    String IP;
    int port, ID, numHosts;
    boolean found;

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
}
