package ecse414.fall2015.group21.game.client.input;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * The state of the mouse in terms of button presses. Mostly reused from the ECSE 321 course project.
 */
public class MouseState {
    private final AtomicLongArray pressTimes = new AtomicLongArray(Button.getCount());
    private final AtomicIntegerArray pressCounts = new AtomicIntegerArray(Button.getCount());
    private float x, y;

    /**
     * Increment press time.
     *
     * @param button the button
     * @param dt the time
     */
    void incrementPressTime(Button button, long dt) {
        pressTimes.getAndAdd(button.ordinal(), dt);
    }

    /**
     * Increment press count.
     *
     * @param button the button
     * @param count the count
     */
    void incrementPressCount(Button button, int count) {
        pressCounts.getAndAdd(button.ordinal(), count);
    }

    /**
     * Sets the mouse x coordinate.
     *
     * @param x The x coordinate
     */
    void setX(float x) {
        this.x = x;
    }

    /**
     * Sets the mouse y coordinate.
     *
     * @param y The y coordinate
     */
    void setY(float y) {
        this.y = y;
    }

    /**
     * Gets the press time.
     *
     * @param button the button
     * @return the press time
     */
    public long getAndClearPressTime(Button button) {
        return pressTimes.getAndSet(button.ordinal(), 0);
    }

    /**
     * Gets the press count.
     *
     * @param button the button
     * @return the press count
     */
    public int getAndClearPressCount(Button button) {
        return pressCounts.getAndSet(button.ordinal(), 0);
    }

    /**
     * Gets the mouse x coordinate, normalized on the screen width
     *
     * @return The x coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the mouse y coordinate, normalized on the screen width
     *
     * @return The y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Clears the entire mouse state.
     */
    public void clearAll() {
        for (int i = 0; i < Button.getCount(); i++) {
            pressTimes.set(i, 0);
            pressCounts.set(i, 0);
        }
    }
}
