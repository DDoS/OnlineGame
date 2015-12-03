package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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

    protected PlayerPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != Type.PLAYER_STATE.id && id != Type.PLAYER_SHOOT.id && id != Type.PLAYER_HEALTH.id) {
            throw new IllegalArgumentException("Not a player packet: " + id);
        }
        type = Type.BY_ID[id];
        time = buf.readLong();
        x = buf.readFloat();
        y = buf.readFloat();
        c = buf.readFloat();
        s = buf.readFloat();
        playerNumber = buf.readShort();
        health = buf.readShort();
    }

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
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id)
                .writeLong(time)
                .writeFloat(x).writeFloat(y)
                .writeFloat(c).writeFloat(s)
                .writeShort(playerNumber)
                .writeShort(health);
    }

    public static class UDP extends PlayerPacket implements Packet.UDP {
        public final int sharedSecret;

        public UDP(ByteBuf buf) {
            super(buf);
            this.sharedSecret = buf.readInt();
        }

        public UDP(Type type, long time, float x, float y, float c, float s, short playerNumber, short health, int sharedSecret) {
            super(type, time, x, y, c, s, playerNumber, health);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(getType().udpLength)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends PlayerPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(Type type, long time, float x, float y, float c, float s, short playerNumber, short health) {
            super(type, time, x, y, c, s, playerNumber, health);
        }
    }
}
