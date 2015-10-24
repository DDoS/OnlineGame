package ecse414.fall2015.group21.game.util;

/**
 * Represents a thread that runs at a specific TPS until terminated.
 * <p>
 * This is a library file from com.flowpowered.commons.ticking
 */
public class TPSLimitedThread extends Thread {
    private final TickingElement element;
    private final Timer timer;
    private volatile boolean running = false;

    public TPSLimitedThread(String name, TickingElement element, int tps) {
        super(name);
        this.element = element;
        timer = new Timer(tps);
    }

    public TPSLimitedThread(ThreadGroup group, String name, TickingElement element, int tps) {
        super(group, name);
        this.element = element;
        timer = new Timer(tps);
    }

    @Override
    public void run() {
        running = true;
        element.onStart();
        timer.start();
        long lastTime = getTime() - (long) (1f / timer.getTps() * 1000000000), currentTime;
        while (running) {
            try {
                element.onTick((currentTime = getTime()) - lastTime);
                lastTime = currentTime;
                timer.sync();
            } catch (Exception ex) {
                System.err.println("Exception in ticking thread, stopping");
                ex.printStackTrace();
                System.out.println("Attempting to stop normally");
                element.onStop();
                return;
            }
        }
        element.onStop();
    }

    public void terminate() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    private static long getTime() {
        return System.nanoTime();
    }
}