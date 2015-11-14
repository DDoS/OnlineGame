package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class ConnectFulfillPacket implements Packet {
    public final short playerNumber;
    public final long seed;

    protected ConnectFulfillPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        playerNumber = buf.readShort();
        seed = buf.readLong();
    }

    protected ConnectFulfillPacket(short playerNumber, long seed) {
        this.playerNumber = playerNumber;
        this.seed = seed;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1 + 2 + 8)
                .writeByte(getType().id)
                .writeShort(playerNumber)
                .writeLong(seed);
    }

    public static class UDP extends ConnectFulfillPacket implements Packet.UDP {
        public UDP(ByteBuf buf) {
            super(buf);
        }

        public UDP(short playerNumber, long seed) {
            super(playerNumber, seed);
        }
    }

    public static class TCP extends ConnectFulfillPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(short playerNumber, long seed) {
            super(playerNumber, seed);
        }
    }
}
