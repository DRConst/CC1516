package Server;

import Commons.Data;

import javax.crypto.SealedObject;

/**
 * Created by drcon on 16/03/2016.
 */
public class Packet {
    short type;
    short version = 1;
    boolean security = false;
    String data;
    SealedObject encryptedData;
    byte options[];

    public Packet(short type, short version, boolean security, String data,SealedObject sealedObject, byte[] options) {
        this.type = type;
        this.version = version;
        this.security = security;
        this.data = data;
        this.options = options;
        this.encryptedData = sealedObject;
    }

    public Packet() {
        options = new byte[4];
    }
}
