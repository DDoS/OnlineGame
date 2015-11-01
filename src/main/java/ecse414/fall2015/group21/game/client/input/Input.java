package ecse414.fall2015.group21.game.client.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ecse414.fall2015.group21.game.client.render.Renderer;
import ecse414.fall2015.group21.game.util.TickingElement;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

/**
 * Input polling thread. Mostly reused from the ECSE 321 course project.
 */
public class Input extends TickingElement {
    private final Runnable stopper;
    private boolean keyboardCreated = false, mouseCreated = false;
    private final Map<Long, KeyboardState> keyboardStates = new ConcurrentHashMap<>();
    private final Map<Long, MouseState> mouseStates = new ConcurrentHashMap<>();
    private final long[] dtKeyPressTimes = new long[Key.getCount()];
    private final int[] dtKeyPressCounts = new int[Key.getCount()];
    private final boolean[] keyPressStates = new boolean[Key.getCount()];
    private final long[] dtButtonPressTimes = new long[Button.getCount()];
    private final int[] dtButtonPressCounts = new int[Button.getCount()];
    private final boolean[] buttonPressStates = new boolean[Button.getCount()];

    /**
     * Instantiates a new input.
     *
     * @param stopper A runnable which stops the game
     */
    public Input(Runnable stopper) {
        super("Input", 60);
        this.stopper = stopper;
    }

    @Override
    public void onStart() {
        Mouse.setGrabbed(true);
    }

    @Override
    public void onTick(long dt) {
        // Check for quit request
        if (Display.isCreated() && Display.isCloseRequested()) {
            stopper.run();
        }
        createInputIfNecessary();
        processKeyboardInput(dt);
        processMouseInput(dt);
        // toggle mouse grabbing
        if (dtKeyPressCounts[Key.ESCAPE.ordinal()] == 1) {
            Mouse.setGrabbed(!Mouse.isGrabbed());
        }
    }

    private void createInputIfNecessary() {
        if (!keyboardCreated) {
            if (Display.isCreated()) {
                if (!Keyboard.isCreated()) {
                    try {
                        Keyboard.create();
                        keyboardCreated = true;
                    } catch (LWJGLException ex) {
                        throw new RuntimeException("Could not create keyboard", ex);
                    }
                } else {
                    keyboardCreated = true;
                }
            }
        }
        if (!mouseCreated) {
            if (Display.isCreated()) {
                if (!Mouse.isCreated()) {
                    try {
                        Mouse.create();
                        mouseCreated = true;
                    } catch (LWJGLException ex) {
                        throw new RuntimeException("Could not create mouse", ex);
                    }
                } else {
                    mouseCreated = true;
                }
            }
        }
    }

    private void processKeyboardInput(long dt) {
        if (keyboardCreated) {
            // Poll the latest keyboard state
            Keyboard.poll();
            // Generate keyboard info for the tick for each key
            for (Key key : Key.values()) {
                final int ordinal = key.ordinal();
                if (key.isDown()) {
                    dtKeyPressTimes[ordinal] = dt;
                    // look for press state rising edge
                    if (!keyPressStates[ordinal]) {
                        // set press count on rising edge
                        dtKeyPressCounts[ordinal] = 1;
                    } else {
                        // no change in key press
                        dtKeyPressCounts[ordinal] = 0;
                    }
                    keyPressStates[ordinal] = true;
                } else {
                    dtKeyPressTimes[ordinal] = 0;
                    dtKeyPressCounts[ordinal] = 0;
                    keyPressStates[ordinal] = false;
                }
            }
            // update the keyboard state objects
            for (KeyboardState state : keyboardStates.values()) {
                for (Key key : Key.values()) {
                    final int ordinal = key.ordinal();
                    state.incrementPressTime(key, dtKeyPressTimes[ordinal]);
                    state.incrementPressCount(key, dtKeyPressCounts[ordinal]);
                }
            }
        }
    }

    private void processMouseInput(long dt) {
        if (mouseCreated) {
            // Poll the latest mouse state
            Mouse.poll();
            // Generate mouse info for the tick for each button
            for (Button button : Button.values()) {
                final int ordinal = button.ordinal();
                if (button.isDown()) {
                    dtButtonPressTimes[ordinal] = dt;
                    // look for press state rising edge
                    if (!buttonPressStates[ordinal]) {
                        // set press count on rising edge
                        dtButtonPressCounts[ordinal] = 1;
                    } else {
                        // no change in button press
                        dtButtonPressCounts[ordinal] = 0;
                    }
                    buttonPressStates[ordinal] = true;
                } else {
                    dtButtonPressTimes[ordinal] = 0;
                    dtButtonPressCounts[ordinal] = 0;
                    buttonPressStates[ordinal] = false;
                }
            }
            final float dx = Mouse.getX() / (float) Renderer.WIDTH;
            final float dy = Mouse.getY() / (float) Renderer.WIDTH;
            // update the mouse state objects
            for (MouseState state : mouseStates.values()) {
                for (Button button : Button.values()) {
                    final int ordinal = button.ordinal();
                    state.incrementPressTime(button, dtButtonPressTimes[ordinal]);
                    state.incrementPressCount(button, dtButtonPressCounts[ordinal]);
                }
                state.setX(dx);
                state.setY(dy);
            }
        }
    }

    @Override
    public void onStop() {
        stopper.run();
        if (Keyboard.isCreated()) {
            Keyboard.destroy();
        }
        keyboardCreated = false;
        if (Mouse.isCreated()) {
            Mouse.destroy();
        }
        mouseCreated = false;
    }

    /**
     * Gets the keyboard state.
     *
     * @return the keyboard state
     */
    public KeyboardState getKeyboardState() {
        // One keyboard state per thread.
        final long callerID = Thread.currentThread().getId();
        KeyboardState state = keyboardStates.get(callerID);
        if (state == null) {
            state = new KeyboardState();
            keyboardStates.put(callerID, state);
        }
        return state;
    }

    /**
     * Gets the mouse state.
     *
     * @return the mouse state
     */
    public MouseState getMouseState() {
        // One keyboard state per thread.
        final long callerID = Thread.currentThread().getId();
        MouseState state = mouseStates.get(callerID);
        if (state == null) {
            state = new MouseState();
            mouseStates.put(callerID, state);
        }
        return state;
    }
}
