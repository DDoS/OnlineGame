package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public abstract class TimeSyncRequestPacket implements Packet {
    protected TimeSyncRequestPacket() {
    }

    @Override
    public Type getType() {
        return Type.TIME_SYNC_REQUEST;
    }

    @Override
    public byte[] asRaw() {
        return new byte[0];
    }

    public static class UDP extends TimeSyncRequestPacket implements Packet.UDP {
        public final int sharedSecret;
        public final int requestNumber;

        public UDP(int sharedSecret, int requestNumber) {
            this.sharedSecret = sharedSecret;
            this.requestNumber = requestNumber;
        }

        @Override
        public byte[] asRaw() {
            return super.asRaw();
        }
    }

    public static class TCP extends TimeSyncRequestPacket implements Packet.TCP {
        public TCP() {
        }
    }
}
