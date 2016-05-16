package Client;

/**
 * Created by NoobLevler on 16/05/2016.
 */
public class ServerUnreachableException extends Throwable {
    public ServerUnreachableException(String message) {
        super(message);
    }

    public ServerUnreachableException() {
    }
}
