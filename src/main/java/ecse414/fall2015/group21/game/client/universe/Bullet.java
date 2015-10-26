package ecse414.fall2015.group21.game.client.universe;

import com.flowpowered.math.vector.Vector2f;

/**
 *
 */
public class Bullet extends Positioned implements Snapshotable<Bullet> {
    private final int number;

    Bullet(int number) {
        this(number, Vector2f.ZERO);
    }

    Bullet(int number, Vector2f position) {
        this.number = number;
        this.position = position;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public Bullet snapshot() {
        return new Bullet(number, position);
    }
}
