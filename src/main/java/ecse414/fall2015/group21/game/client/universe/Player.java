package ecse414.fall2015.group21.game.client.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector3f;

/**
 * Represents a player in the universe, with a position, rotation and acceleration. This is a simple data class.
 */
public class Player {
    private final int number;
    private Vector3f position;
    private Vector3f speed;
    private Complexf rotation;
    private Vector3f acceleration;

    Player(int number) {
        this(number, Vector3f.ZERO, Complexf.IDENTITY, Vector3f.ZERO, Vector3f.ZERO);
    }

    Player(int number, Vector3f position, Complexf rotation, Vector3f speed, Vector3f acceleration) {
        this.number = number;
        this.position = position;
        this.rotation = rotation;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    public int getNumber() {
        return number;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Complexf getRotation() {
        return rotation;
    }

    public Vector3f getSpeed() {
        return speed;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    void setPosition(Vector3f position) {
        this.position = position;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }

    void setSpeed(Vector3f speed) {
        this.speed = speed;
    }

    void setAcceleration(Vector3f acceleration) {
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
