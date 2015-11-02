package ecse414.fall2015.group21.game.server.universe;

import java.util.concurrent.atomic.AtomicInteger;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 *
 */
public class Bullet extends Positioned implements Snapshotable<Bullet> {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final int internalID;
    private final int number;

    public Bullet(int number) {
        this(number, Vector2f.ZERO, Complexf.IDENTITY);
    }

    public Bullet(int number, Vector2f position, Complexf rotation) {
        this(ID_COUNTER.getAndIncrement(), number, position, rotation);
    }

    private Bullet(int internalID, int number, Vector2f position, Complexf rotation) {
        super(position, rotation);
        this.internalID = internalID;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public Bullet snapshot() {
        return new Bullet(internalID, number, position, rotation);
    }

    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof Bullet && internalID == ((Bullet) that).internalID;
    }

    @Override
    public int hashCode() {
        return internalID;
    }
}
