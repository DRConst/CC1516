package Server;

import Commons.Data;

/**
 * Created by drcon on 16/03/2016.
 */
public class Packet {
    short type;
    short version = 1;
    boolean security = false;
    Data data;
    byte options[];

    public Packet(short type, short version, boolean security, Data data, byte[] options) {
        this.type = type;
        this.version = version;
        this.security = security;
        this.data = data;
        this.options = options;
    }

    public Packet() {
        options = new byte[4];
    }
}
