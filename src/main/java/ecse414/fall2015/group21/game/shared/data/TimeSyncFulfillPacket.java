package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class TimeSyncFulfillPacket implements Packet {
    public final long time;

    protected TimeSyncFulfillPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        time = buf.readLong();
    }

    protected TimeSyncFulfillPacket(long time) {
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.TIME_SYNC_FULFILL;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1 + 8)
                .writeByte(getType().id)
                .writeLong(time);
    }

    public static class UDP extends TimeSyncFulfillPacket implements Packet.UDP {
        public final int requestNumber;

        public UDP(ByteBuf buf) {
            super(buf);
            requestNumber = buf.readInt();
        }

        public UDP(long time, int requestNumber) {
            super(time);
            this.requestNumber = requestNumber;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(buf.capacity() + 4)
                    .writeInt(requestNumber);
        }
    }

    public static class TCP extends TimeSyncFulfillPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(long time) {
            super(time);
        }
    }
}
