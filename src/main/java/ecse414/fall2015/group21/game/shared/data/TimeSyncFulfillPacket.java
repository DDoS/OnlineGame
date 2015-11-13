package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public abstract class TimeSyncFulfillPacket implements Packet {
    public final long time;

    protected TimeSyncFulfillPacket(long time) {
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.TIME_SYNC_FULFILL;
    }

    @Override
    public byte[] asRaw() {
        return new byte[0];
    }

    public static class UDP extends TimeSyncFulfillPacket implements Packet.UDP {
        public final int requestNumber;

        public UDP(long time, int requestNumber) {
            super(time);
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
