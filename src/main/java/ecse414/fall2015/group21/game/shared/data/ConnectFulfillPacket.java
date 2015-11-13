package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public abstract class ConnectFulfillPacket implements Packet {
    public final short playerNumber;
    public final long seed;

    protected ConnectFulfillPacket(short playerNumber, long seed) {
        this.playerNumber = playerNumber;
        this.seed = seed;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }

    @Override
    public byte[] asRaw() {
        return new byte[0];
    }

    public static class UDP extends ConnectFulfillPacket implements Packet.UDP {
        public UDP(short playerNumber, long seed) {
            super(playerNumber, seed);
        }
    }

    public static class TCP extends ConnectFulfillPacket implements Packet.TCP {
        public TCP(short playerNumber, long seed) {
            super(playerNumber, seed);
        }
    }
}
