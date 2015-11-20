package ecse414.fall2015.group21.game.shared.data;

/**
 *
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
