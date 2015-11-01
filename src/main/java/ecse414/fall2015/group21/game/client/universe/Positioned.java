package ecse414.fall2015.group21.game.client.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Anything that has a position in 2D space.
 */
public abstract class Positioned {
    protected Vector2f position;
    protected Complexf rotation;

    protected Positioned(Vector2f position, Complexf rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector2f getPosition() {
        return position;
    }

    void setPosition(Vector2f position) {
        this.position = position;
    }

    public Complexf getRotation() {
        return rotation;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }
}
