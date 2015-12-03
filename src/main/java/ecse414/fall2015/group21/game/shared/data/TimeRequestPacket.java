package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * TimeRequestPacket is a Packet type used to request the time from the server for synchronization.
 * There are implementations for both UDP and TCP Packets.
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
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id)
                .writeInt(requestNumber);
    }

    public static class UDP extends TimeRequestPacket implements Packet.UDP {
        public final int sharedSecret;

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
            return buf.capacity(getType().udpLength)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends TimeRequestPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(int requestNumber) {
            super(requestNumber);
        }
    }
}
