package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public interface Packet {
    Type getType();

    byte[] asRaw();

    interface UDP extends Packet {
    }

    interface TCP extends Packet {
    }

    enum Type {
        CONNECT_REQUEST(0),
        CONNECT_FULFILL(1),
        TIME_SYNC_REQUEST(2),
        TIME_SYNC_FULFILL(3),
        PLAYER_STATE(4),
        PLAYER_SHOOT(5),
        PLAYER_HEALTH(6);
        public final int id;

        Type(int id) {
            this.id = id;
        }
    }
}
