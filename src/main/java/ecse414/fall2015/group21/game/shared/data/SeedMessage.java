package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class SeedMessage implements Message {
    public final long seed;

    public SeedMessage(long seed) {
        this.seed = seed;
    }

    @Override
    public Type getType() {
        return Type.SEED;
    }
}
