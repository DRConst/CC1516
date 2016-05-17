package Commons;

import java.io.Serializable;

/**
 * Created by drcon on 16/03/2016.
 */
public class ConReqData extends Data implements Serializable {
    String songName;

    public ConReqData(String songName) {
        this.songName = songName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
