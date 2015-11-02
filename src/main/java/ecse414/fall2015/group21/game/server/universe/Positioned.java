package ecse414.fall2015.group21.game.server.universe;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Anything that has a position in 2D space and time.
 */
public abstract class Positioned {
    protected long time;
    protected Vector2f position;
    protected Complexf rotation;

    protected Positioned(long time, Vector2f position, Complexf rotation) {
        this.time = time;
        this.position = position;
        this.rotation = rotation;
    }

    public long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
    }

    public Vector2f getPosition() {
        return position;
    }

    void setPosition(Vector2f position) {
        this.position = position;
    }

    void setTimePosition(long time, Vector2f position) {
        this.time = time;
        this.position = position;
    }

    public Complexf getRotation() {
        return rotation;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }
}
