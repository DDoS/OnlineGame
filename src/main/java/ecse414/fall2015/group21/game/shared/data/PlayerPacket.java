package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public abstract class PlayerPacket implements Packet {
    public final Type type;
    public final long time;
    public final float x, y;
    public final float c, s;
    public final short playerNumber;
    public final short health;

    protected PlayerPacket(Type type, long time, float x, float y, float c, float s, short playerNumber, short health) {
        if (type != Type.PLAYER_STATE && type != Type.PLAYER_SHOOT && type != Type.PLAYER_HEALTH) {
            throw new IllegalArgumentException("Not a player packet: " + type.name());
        }
        this.type = type;
        this.time = time;
        this.x = x;
        this.y = y;
        this.c = c;
        this.s = s;
        this.playerNumber = playerNumber;
        this.health = health;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public byte[] asRaw() {
        return new byte[0];
    }

    public static class UDP extends PlayerPacket implements Packet.UDP {
        public final int sharedSecret;

        public UDP(Type type, int sharedSecret, long time, float x, float y, float c, float s, short playerNumber, short health) {
            super(type, time, x, y, c, s, playerNumber, health);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public byte[] asRaw() {
            return super.asRaw();
        }
    }

    public static class TCP extends PlayerPacket implements Packet.TCP {
        public TCP(Type type, long time, float x, float y, float c, float s, short playerNumber, short health) {
            super(type, time, x, y, c, s, playerNumber, health);
        }
    }
}
