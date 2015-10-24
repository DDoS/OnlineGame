package ecse414.fall2015.group21.game;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import ecse414.fall2015.group21.game.client.world.World;

/**
 * Base class for the client and server processes. Mostly reused from the ECSE 321 course project.
 */
public abstract class Game {
    // A semaphore with no permits, so that the first acquire() call blocks
    private final Semaphore semaphore = new Semaphore(0);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final World world;

    /**
     * Instantiates a new game with only a world.
     */
    protected Game() {
        world = new World(this);
    }

    /**
     * Starts the process.
     */
    protected void start() {
        world.start();
    }

    /**
     * Stops the process.
     */
    protected void stop() {
        world.stop();
    }

    /**
     * Gets the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Starts the game and causes the current thread to wait until the {@link #close()} method is called. When this happens, the thread resumes and the game is stopped. Interrupting the thread will
     * not cause it to close, only calling {@link #close()} will. Calls to {@link #close()} before open() are not counted.
     */
    public void open() {
        // Only start the game if running has a value of false, in which case it's set to true and the if statement passes
        if (running.compareAndSet(false, true)) {
            // Start the threads, which might release permits by calling close() before all are started
            start();
            // Attempts to acquire a permit, but since none are available (except for the situation stated above), the thread blocks
            semaphore.acquireUninterruptibly();
            // A permit was acquired, which means close() was called; so we stop game. The available permit count returns to zero
            stop();
        }
    }

    /**
     * Wakes up the thread that has opened the game (by having called {@link #open()}) and allows it to resume it's activity to trigger the end of the game.
     */
    public void close() {
        // Only stop the game if running has a value of true, in which case it's set to false and the if statement passes
        if (running.compareAndSet(true, false)) {
            // Release a permit (which doesn't need to be held by the thread in the first place),
            // allowing the main thread to acquire one and resume to close the game
            semaphore.release();
            // The available permit count is now non-zero
        }
    }

    /**
     * Returns true if the game is running, false if otherwise.
     *
     * @return Whether or not the game is running
     */
    public boolean isRunning() {
        return running.get();
    }
}
