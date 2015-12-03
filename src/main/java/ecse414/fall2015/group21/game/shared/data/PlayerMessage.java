package ecse414.fall2015.group21.game.shared.data;

import ecse414.fall2015.group21.game.server.universe.Player;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * PlayerMessage contains player game information, such as position in the game, rotation of the player and player health.
 */
public class PlayerMessage implements Message {
    public final Type type;
    public final long time;
    public final Vector2f position;
    public final Complexf rotation;
    public final short health;
    public final int playerNumber;

    public PlayerMessage(Type type, Player player, boolean died) {
        this(type, player.getTime(), player.getPosition(), player.getRotation(), (short) (died ? 0 : 1), player.getNumber());
    }

    public PlayerMessage(Type type, long time, Vector2f position, Complexf rotation, short health, int playerNumber) {
        if (type != Type.PLAYER_STATE && type != Type.PLAYER_SHOOT && type != Type.PLAYER_HEALTH) {
            throw new IllegalArgumentException("Not a player message: " + type.name());
        }
        this.type = type;
        this.time = time;
        this.position = position;
        this.rotation = rotation;
        this.health = health;
        this.playerNumber = playerNumber;
    }

    @Override
    public Type getType() {
        return type;
    }
}
