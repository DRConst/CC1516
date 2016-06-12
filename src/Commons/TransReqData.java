package Commons;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by NoobLevler on 12/06/2016.
 */
public class TransReqData implements Serializable{
    public String file;
    public int port;
    public InetAddress ip;

    public TransReqData(String file, int port, InetAddress ip) {
        this.file = file;
        this.port = port;
        this.ip = ip;
    }
}
