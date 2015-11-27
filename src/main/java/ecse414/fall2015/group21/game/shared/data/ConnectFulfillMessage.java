package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class ConnectFulfillMessage implements Message {
    public final short playerNumber;
    public final long seed;
    public final long time;

    public ConnectFulfillMessage(short playerNumber, long seed, long time) {
        this.playerNumber = playerNumber;
        this.seed = seed;
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }
}
