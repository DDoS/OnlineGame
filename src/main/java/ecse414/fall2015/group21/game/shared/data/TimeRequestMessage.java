package ecse414.fall2015.group21.game.shared.data;

/**
 * TimeRequestMessage is a message type used to request the time from the server for synchronization.
 */
public class TimeRequestMessage implements Message {
    public final int requestNumber;

    public TimeRequestMessage(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    @Override
    public Type getType() {
        return Type.TIME_REQUEST;
    }
}
