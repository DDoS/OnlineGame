package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public interface Message {
    Type getType();

    enum Type {
        CONNECT_REQUEST,
        CONNECT_FULFILL,
        TIME_REQUEST,
        TIME_FULFILL,
        PLAYER_STATE,
        PLAYER_SHOOT,
        PLAYER_HEALTH;

        public static Type fromPacketType(Packet.Type packetType) {
            switch (packetType) {
                case CONNECT_REQUEST:
                    return Type.CONNECT_REQUEST;
                case CONNECT_FULFILL:
                    return Type.CONNECT_FULFILL;
                case TIME_REQUEST:
                    return Type.TIME_REQUEST;
                case TIME_FULFILL:
                    return Type.TIME_FULFILL;
                case PLAYER_STATE:
                    return Type.PLAYER_STATE;
                case PLAYER_SHOOT:
                    return Type.PLAYER_SHOOT;
                case PLAYER_HEALTH:
                    return Type.PLAYER_HEALTH;
                default:
                    throw new IllegalArgumentException("Not a packet type: " + packetType.name());
            }
        }
    }
}
