package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public abstract class ConnectRequestPacket implements Packet {
    protected ConnectRequestPacket() {
    }

    @Override
    public Type getType() {
        return Type.CONNECT_REQUEST;
    }

    @Override
    public byte[] asRaw() {
        return new byte[0];
    }

    public static class UDP extends ConnectRequestPacket implements Packet.UDP {
        public final int ipAddress;
        public final short port;
        public final int sharedSecret;

        public UDP(int ipAddress, short port, int sharedSecret) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.sharedSecret = sharedSecret;
        }

        @Override
        public byte[] asRaw() {
            return super.asRaw();
        }
    }

    public static class TCP extends ConnectRequestPacket implements Packet.TCP {
        public TCP() {
        }
    }
}
