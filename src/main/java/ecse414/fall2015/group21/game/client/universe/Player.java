package ecse414.fall2015.group21.game.client.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Represents a player in the universe, with a position, rotation and acceleration. This is a simple data class.
 */
public class Player {
    private final int number;
    private Vector2f position;
    private Vector2f speed;
    private Complexf rotation;
    private Vector2f acceleration;

    Player(int number) {
        this(number, Vector2f.ZERO, Complexf.IDENTITY, Vector2f.ZERO, Vector2f.ZERO);
    }

    Player(int number, Vector2f position, Complexf rotation, Vector2f speed, Vector2f acceleration) {
        this.number = number;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    public int getNumber() {
        return number;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Complexf getRotation() {
        return rotation;
    }

    public Vector2f getSpeed() {
        return speed;
    }

    public Vector2f getAcceleration() {
        return acceleration;
    }

    void setPosition(Vector2f position) {
        this.position = position;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }

    void setSpeed(Vector2f speed) {
        this.speed = speed;
    }

    void setAcceleration(Vector2f acceleration) {
        this.acceleration = acceleration;
    }

    Player snapshot() {
        return new Player(number, position, rotation, speed, acceleration);
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
