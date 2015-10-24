package ecse414.fall2015.group21.game.util;

/**
 * Represents an element that ticks at a specific TPS.
 * <p>
 * This is a library file from com.flowpowered.commons.ticking
 */
public abstract class TickingElement {
    private final String name;
    private final int tps;
    private final ThreadGroup group;
    private volatile TPSLimitedThread thread;

    public TickingElement(String name, int tps) {
        this.name = name;
        this.tps = tps;
        this.group = new ThreadGroup(name + " ThreadGroup");
    }

    public final void start() {
        synchronized (this) {
            if (thread == null) {
                thread = new TPSLimitedThread(group, name, this, tps);
                thread.start();
            }
        }
    }

    public final void stop() {
        synchronized (this) {
            if (thread != null) {
                thread.terminate();
                thread = null;
            }
        }
    }

    public final boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    public TPSLimitedThread getThread() {
        return thread;
    }

    public ThreadGroup getGroup() {
        return group;
    }

    public abstract void onStart();

    public abstract void onTick(long dt);

    public abstract void onStop();
}
