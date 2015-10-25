package ecse414.fall2015.group21.game.client.input;

import org.lwjgl.input.Keyboard;

/**
 * Keys used as input by the game. Reused from the ECSE 321 course project.
 */
public enum Key {

    /**
     * The up key.
     */
    UP(Keyboard.KEY_W),
    /**
     * The down key.
     */
    DOWN(Keyboard.KEY_S),
    /**
     * The left key.
     */
    LEFT(Keyboard.KEY_A),
    /**
     * The right key.
     */
    RIGHT(Keyboard.KEY_D),
    /**
     * The escape key.
     */
    ESCAPE(Keyboard.KEY_ESCAPE);
    private static final int COUNT = values().length;
    private final int keyCode;

    Key(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * Gets the key code.
     *
     * @return the key code
     */
    int getKeyCode() {
        return keyCode;
    }

    /**
     * Checks if is key is down.
     *
     * @return true, if key is down
     */
    boolean isDown() {
        return Keyboard.isKeyDown(keyCode);
    }

    /**
     * Gets the key count.
     *
     * @return the key count
     */
    public static int getCount() {
        return COUNT;
    }
}
