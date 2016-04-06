package Server;

/**
 * Created by drcon on 06/04/2016.
 */
public class LoginFailedException extends Throwable {
    public LoginFailedException() {
    }

    public LoginFailedException(String s) {
        super(s);
    }

    public LoginFailedException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public LoginFailedException(Throwable throwable) {
        super(throwable);
    }

    public LoginFailedException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
