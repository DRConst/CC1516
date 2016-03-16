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
}
