package Commons;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by drcon on 16/03/2016.
 */
public class ProResData extends Data implements Serializable {
    Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
