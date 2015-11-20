package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class ConnectFulfillMessage implements Message {
    public final short playerNumber;
    public final long seed;

    public ConnectFulfillMessage(short playerNumber, long seed) {
        this.playerNumber = playerNumber;
        this.seed = seed;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }
}
