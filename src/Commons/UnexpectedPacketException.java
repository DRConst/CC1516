package Commons;

/**
 * Created by NoobLevler on 17/05/2016.
 */
public class UnexpectedPacketException extends Throwable {
    public UnexpectedPacketException() {
    }

    public UnexpectedPacketException(String message) {
        super(message);
    }
}
