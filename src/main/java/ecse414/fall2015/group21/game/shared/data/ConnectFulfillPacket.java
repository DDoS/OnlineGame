package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class ConnectFulfillPacket implements Packet {
    public final short playerNumber;
    public final long seed;
    public final long time;

    protected ConnectFulfillPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        playerNumber = buf.readShort();
        seed = buf.readLong();
        time = buf.readLong();
    }

    protected ConnectFulfillPacket(short playerNumber, long seed, long time) {
        this.playerNumber = playerNumber;
        this.seed = seed;
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id)
                .writeShort(playerNumber)
                .writeLong(seed)
                .writeLong(time);
    }

    public static class UDP extends ConnectFulfillPacket implements Packet.UDP {
        public final int sharedSecret;

        public UDP(ByteBuf buf) {
            super(buf);
            sharedSecret = buf.readInt();
        }

        public UDP(short playerNumber, long seed, long time, int sharedSecret) {
            super(playerNumber, seed, time);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(getType().udpLength)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends ConnectFulfillPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(short playerNumber, long seed, long time) {
            super(playerNumber, seed, time);
        }
    }
}
