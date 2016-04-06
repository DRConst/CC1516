package Server;

import Commons.Data;

import javax.crypto.SealedObject;
import java.io.Serializable;

/**
 * Created by drcon on 16/03/2016.
 */
public class Packet implements Serializable{
    short type;
    int version = 1;
    boolean security = false;
    String data;
    SealedObject encryptedData;
    byte options[];

    public Packet(short type, int version, boolean security, String data,SealedObject sealedObject, byte[] options) {
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

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public SealedObject getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(SealedObject encryptedData) {
        this.encryptedData = encryptedData;
    }

    public byte[] getOptions() {
        return options;
    }

    public void setOptions(byte[] options) {
        this.options = options;
    }
}
