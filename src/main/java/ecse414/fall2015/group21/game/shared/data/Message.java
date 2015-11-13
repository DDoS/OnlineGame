package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public interface Message {
    Type getType();

    enum Type {
        SEED,
        TIME,
        PLAYER_STATE,
        PLAYER_SHOOT,
        PLAYER_HEALTH
    }
}
