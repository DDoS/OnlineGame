package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class TimeRequestPacket implements Packet {
    public final int requestNumber;

    protected TimeRequestPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        requestNumber = buf.readInt();
    }

    protected TimeRequestPacket(int requestNumber) {
        this.requestNumber = requestNumber;
    }

    @Override
    public Type getType() {
        return Type.TIME_REQUEST;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1)
                .writeByte(getType().id)
                .writeInt(requestNumber);
    }

    public static class UDP extends TimeRequestPacket implements Packet.UDP {
        public final int sharedSecret;

        static {
            FACTORY.register(TimeRequestPacket.UDP.class, Type.TIME_REQUEST);
        }

        public UDP(ByteBuf buf) {
            super(buf);
            sharedSecret = buf.readInt();
        }

        public UDP(int requestNumber, int sharedSecret) {
            super(requestNumber);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(buf.capacity() + 4)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends TimeRequestPacket implements Packet.TCP {
        static {
            FACTORY.register(TimeRequestPacket.TCP.class, Type.TIME_REQUEST);
        }

        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(int requestNumber) {
            super(requestNumber);
        }
    }
}
