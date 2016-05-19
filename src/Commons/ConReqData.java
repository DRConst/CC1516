package Commons;

import java.io.Serializable;

/**
 * Created by drcon on 16/03/2016.
 */
public class ConReqData extends Data implements Serializable {
    String songName;
    boolean server;

    public ConReqData(String songName) {

        this.songName = songName;
        this.server = false;
    }

    public ConReqData(String songName, boolean server) {
        this.songName = songName;
        this.server = server;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }
}
