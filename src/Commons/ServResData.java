package Commons;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by NoobLevler on 12/06/2016.
 */
public class ServResData implements Serializable{

    private ArrayList<Integer> secondaryServerPorts;
    private ArrayList<InetAddress> secondaryServerIPs;

    public ServResData(ArrayList<Integer> secondaryServerPorts, ArrayList<InetAddress> secondaryServerIPs) {
        this.secondaryServerPorts = secondaryServerPorts;
        this.secondaryServerIPs = secondaryServerIPs;
    }

    public ArrayList<Integer> getSecondaryServerPorts() {
        return secondaryServerPorts;
    }

    public void setSecondaryServerPorts(ArrayList<Integer> secondaryServerPorts) {
        this.secondaryServerPorts = secondaryServerPorts;
    }

    public ArrayList<InetAddress> getSecondaryServerIPs() {
        return secondaryServerIPs;
    }

    public void setSecondaryServerIPs(ArrayList<InetAddress> secondaryServerIPs) {
        this.secondaryServerIPs = secondaryServerIPs;
    }
}
