package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class TimeFulfillPacket implements Packet {
    public final int requestNumber;
    public final long time;

    protected TimeFulfillPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        requestNumber = buf.readInt();
        time = buf.readLong();
    }

    protected TimeFulfillPacket(int requestNumber, long time) {
        this.requestNumber = requestNumber;
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.TIME_FULFILL;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1 + 4 + 8)
                .writeByte(getType().id)
                .writeInt(requestNumber)
                .writeLong(time);
    }

    public static class UDP extends TimeFulfillPacket implements Packet.UDP {

        static {
            FACTORY.register(TimeFulfillPacket.UDP.class, Type.TIME_FULFILL);
        }

        public UDP(ByteBuf buf) {
            super(buf);
        }

        public UDP(int requestNumber, long time) {
            super(requestNumber, time);
        }
    }

    public static class TCP extends TimeFulfillPacket implements Packet.TCP {
        static {
            FACTORY.register(TimeFulfillPacket.TCP.class, Type.TIME_FULFILL);
        }

        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(int requestNumber, long time) {
            super(requestNumber, time);
        }
    }
}