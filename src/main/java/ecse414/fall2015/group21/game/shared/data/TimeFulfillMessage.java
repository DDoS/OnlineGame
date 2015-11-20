package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class TimeFulfillMessage implements Message {
    public final int requestNumber;
    public final long time;

    public TimeFulfillMessage(int requestNumber, long time) {
        this.requestNumber = requestNumber;
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.TIME_FULFILL;
    }
}
