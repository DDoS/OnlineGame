package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class TimeSyncRequestPacket implements Packet {
    protected TimeSyncRequestPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
    }

    protected TimeSyncRequestPacket() {
    }

    @Override
    public Type getType() {
        return Type.TIME_SYNC_REQUEST;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1)
                .writeByte(getType().id);
    }

    public static class UDP extends TimeSyncRequestPacket implements Packet.UDP {
        public final int sharedSecret;
        public final int requestNumber;

        static {
            FACTORY.register(TimeSyncRequestPacket.UDP.class, Type.TIME_SYNC_REQUEST);
        }

        public UDP(ByteBuf buf) {
            super(buf);
            sharedSecret = buf.readInt();
            requestNumber = buf.readInt();
        }

        public UDP(int sharedSecret, int requestNumber) {
            this.sharedSecret = sharedSecret;
            this.requestNumber = requestNumber;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(buf.capacity() + 4 + 4)
                    .writeInt(sharedSecret)
                    .writeInt(requestNumber);
        }
    }

    public static class TCP extends TimeSyncRequestPacket implements Packet.TCP {
        static {
            FACTORY.register(TimeSyncRequestPacket.TCP.class, Type.TIME_SYNC_REQUEST);
        }

        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP() {
        }
    }
}
