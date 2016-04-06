package Server;

/**
 * Created by drcon on 06/04/2016.
 */
public class UserRegisteredException extends Throwable {
    public UserRegisteredException() {
    }

    public UserRegisteredException(String s) {
        super(s);
    }

    public UserRegisteredException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public UserRegisteredException(Throwable throwable) {
        super(throwable);
    }

    public UserRegisteredException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
