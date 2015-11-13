package ecse414.fall2015.group21.game.server.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Represents a player in the universe, with a position, rotation and acceleration. This is a simple data class.
 */
public class Player extends Positioned implements Snapshotable<Player> {
    private final short number;

    public Player(short number, long time) {
        this(number, time, Vector2f.ZERO, Complexf.IDENTITY);
    }

    public Player(short number, long time, Vector2f position, Complexf rotation) {
        super(time, position, rotation);
        this.number = number;
    }

    public short getNumber() {
        return number;
    }

    @Override
    public Player snapshot() {
        return new Player(number, time, position, rotation);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Player && number == ((Player) other).number;
    }

    @Override
    public int hashCode() {
        return number;
    }
}
