package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class TimeMessage implements Message {
    public final long time;

    public TimeMessage(long time) {
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.TIME;
    }
}
