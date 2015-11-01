package ecse414.fall2015.group21.game.server.universe;

/**
 * An object that can be snapshoted. A snapshot is an immutable representation of the state at the time it was taken.
 */
public interface Snapshotable<T> {
    /**
     * Takes a snapshot of the object.
     *
     * @return the snapshot
     */
    T snapshot();
}
