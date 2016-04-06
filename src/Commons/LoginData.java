package Commons;

import java.io.Serializable;

/**
 * Created by drcon on 06/04/2016.
 */
public class LoginData extends Data implements Serializable {
    String username;
    String password;

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
