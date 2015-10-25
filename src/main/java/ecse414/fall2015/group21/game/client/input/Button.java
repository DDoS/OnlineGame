package ecse414.fall2015.group21.game.client.input;

import org.lwjgl.input.Mouse;

/**
 * Keys used as input by the game. Reused from the ECSE 321 course project.
 */
public enum Button {

    /**
     * The left button.
     */
    LEFT(0);
    private static final int COUNT = values().length;
    private final int buttonCode;

    Button(int buttonCode) {
        this.buttonCode = buttonCode;
    }

    /**
     * Gets the button code.
     *
     * @return the button code
     */
    int getButtonCode() {
        return buttonCode;
    }

    /**
     * Checks if is button is down.
     *
     * @return true, if button is down
     */
    boolean isDown() {
        return Mouse.isButtonDown(buttonCode);
    }

    /**
     * Gets the button count.
     *
     * @return the button count
     */
    public static int getCount() {
        return COUNT;
    }
}
