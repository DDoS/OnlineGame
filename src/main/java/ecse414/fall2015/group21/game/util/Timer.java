package ecse414.fall2015.group21.game.util;

import java.util.Arrays;

import org.lwjgl.LWJGLUtil;

/**
 * A time class. Calling the {@link #sync()} method at the end of each tick will cause the thread to sleep for the correct time delay between the ticks. {@link #start()} must be called just before the
 * loop to start the timer. {@link #reset()} is used to reset the start time to the current time. <br> Based on LWJGL's implementation of {@link org.lwjgl.opengl.Sync}.
 * <p>
 * This is a library file from com.flowpowered.commons.ticking
 */
public class Timer {
    // Time to sleep or yield before next tick
    private long nextTick = -1;
    // Last 10 running averages for sleeps and yields
    private final RunAverages sleepDurations = new RunAverages(10, 1000000);
    private final RunAverages yieldDurations = new RunAverages(10, (int) (-(getTime() - getTime()) * 1.333f));
    // The target tps
    private final int tps;

    static {
        // Makes windows thread sleeping more accurate
        if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS) {
            final Thread sleepingDaemon = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (Exception ignored) {
                    }
                }
            };
            sleepingDaemon.setName("Timer");
            sleepingDaemon.setDaemon(true);
            sleepingDaemon.start();
        }
    }

    /**
     * Constructs a new timer.
     *
     * @param tps The target tps
     */
    public Timer(int tps) {
        this.tps = tps;
    }

    /**
     * Returns the timer's target TPS.
     *
     * @return The tps
     */
    public int getTps() {
        return tps;
    }

    /**
     * Starts the timer.
     */
    public void start() {
        nextTick = getTime();
    }

    /**
     * Resets the timer.
     */
    public void reset() {
        start();
    }

    /**
     * An accurate sync method that will attempt to run at the tps. It should be called once every tick.
     */
    public void sync() {
        if (nextTick < 0) {
            throw new IllegalStateException("Timer was not started");
        }
        if (tps <= 0) {
            return;
        }
        try {
            // Sleep until the average sleep time is greater than the time remaining until next tick
            for (long time1 = getTime(), time2; nextTick - time1 > sleepDurations.average(); time1 = time2) {
                Thread.sleep(1);
                // Update average sleep time
                sleepDurations.add((time2 = getTime()) - time1);
            }
            // Slowly dampen sleep average if too high to avoid yielding too much
            sleepDurations.dampen();
            // Yield until the average yield time is greater than the time remaining until next tick
            for (long time1 = getTime(), time2; nextTick - time1 > yieldDurations.average(); time1 = time2) {
                Thread.yield();
                // Update average yield time
                yieldDurations.add((time2 = getTime()) - time1);
            }
        } catch (InterruptedException ignored) {
        }
        // Schedule next frame, drop frames if it's too late for next frame
        nextTick = Math.max(nextTick + 1000000000 / tps, getTime());
    }

    // Get the system time in nanoseconds
    private static long getTime() {
        return System.nanoTime();
    }

    // Holds a number of run times for averaging
    private static class RunAverages {
        // Dampen threshold, 10ms
        private static final long DAMPEN_THRESHOLD = 10000000;
        // Dampen factor, don't alter this value
        private static final float DAMPEN_FACTOR = 0.9f;
        private final long[] values;
        private int currentIndex = 0;

        private RunAverages(int slotCount, long initialValue) {
            values = new long[slotCount];
            Arrays.fill(values, initialValue);
        }

        private void add(long value) {
            currentIndex %= values.length;
            values[currentIndex++] = value;
        }

        private long average() {
            long sum = 0;
            for (long slot : values) {
                sum += slot;
            }
            return sum / values.length;
        }

        private void dampen() {
            if (average() > DAMPEN_THRESHOLD) {
                for (int i = 0; i < values.length; i++) {
                    values[i] *= DAMPEN_FACTOR;
                }
            }
        }
    }
}