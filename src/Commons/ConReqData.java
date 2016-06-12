package Commons;

import java.io.Serializable;

/**
 * Created by drcon on 16/03/2016.
 */
public class ConReqData extends Data implements Serializable {
    private String songName;
    private boolean server;
    private boolean propagate;

    public ConReqData(String songName) {

        this.songName = songName;
        this.server = false;
        this.propagate = true;
    }

    public ConReqData(String songName, boolean server) {
        this.songName = songName;
        this.server = server;
        this.propagate = true;
    }

    public ConReqData(String songName, boolean server, boolean propagate) {
        this.songName = songName;
        this.server = server;
        this.propagate = propagate;
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

    public boolean isPropagate() {
        return propagate;
    }

    public void setPropagate(boolean propagate) {
        this.propagate = propagate;
    }
}
