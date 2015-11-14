package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 *
 */
public abstract class ConnectRequestPacket implements Packet {
    protected ConnectRequestPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
    }

    protected ConnectRequestPacket() {
    }

    @Override
    public Type getType() {
        return Type.CONNECT_REQUEST;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(1)
                .writeByte(getType().id);
    }

    public static class UDP extends ConnectRequestPacket implements Packet.UDP {
        public final int ipAddress;
        public final short port;
        public final int sharedSecret;

        static {
            FACTORY.register(ConnectRequestPacket.UDP.class, Type.CONNECT_REQUEST);
        }

        public UDP(ByteBuf buf) {
            super(buf);
            ipAddress = buf.readInt();
            port = buf.readShort();
            sharedSecret = buf.readInt();
        }

        public UDP(int ipAddress, short port, int sharedSecret) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(buf.capacity() + 4 + 2 + 4)
                    .writeInt(ipAddress)
                    .writeShort(port)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends ConnectRequestPacket implements Packet.TCP {
        static {
            FACTORY.register(ConnectRequestPacket.TCP.class, Type.CONNECT_REQUEST);
        }

        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP() {
        }
    }
}
