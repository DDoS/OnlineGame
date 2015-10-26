package ecse414.fall2015.group21.game.client.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Represents a player in the universe, with a position, rotation and acceleration. This is a simple data class.
 */
public class Player extends Positioned implements Snapshotable<Player> {
    private final int number;
    private Complexf rotation;

    Player(int number) {
        this(number, Vector2f.ZERO, Complexf.IDENTITY);
    }

    Player(int number, Vector2f position, Complexf rotation) {
        this.number = number;
        this.position = position;
        this.rotation = rotation;
    }

    public int getNumber() {
        return number;
    }

    public Complexf getRotation() {
        return rotation;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }

    @Override
    public Player snapshot() {
        return new Player(number, position, rotation);
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
