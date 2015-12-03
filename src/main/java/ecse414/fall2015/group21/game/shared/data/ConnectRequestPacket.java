package ecse414.fall2015.group21.game.shared.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * ConnectRequestPacket is a Packet type used to request a connection to the server.
 * There are implementations for both UDP and TCP Packets.
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
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id);
    }

    public static class UDP extends ConnectRequestPacket implements Packet.UDP {
        public UDP(ByteBuf buf) {
            super(buf);
        }

        public UDP() {
        }
    }

    public static class TCP extends ConnectRequestPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP() {
        }
    }
}
