package ecse414.fall2015.group21.game.client.universe;

import ecse414.fall2015.group21.game.client.input.Button;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * The game physics, holds all the game state.
 */
public class RemoteUniverse extends Universe {
    private static final DirectionKey[] DIRECTION_KEYS = {
            new DirectionKey(Key.DOWN, new Vec2(0, -1)),
            new DirectionKey(Key.UP, new Vec2(0, 1)),
            new DirectionKey(Key.LEFT, new Vec2(-1, 0)),
            new DirectionKey(Key.RIGHT, new Vec2(1, 0))
    };
    private final Input input;
    private Player mainPlayer;
    private Body mainPlayerBody;

    public RemoteUniverse(Input input) {
        this.input = input;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add client player
        mainPlayer = new Player(0, getTime(), Vector2f.ONE, Complexf.IDENTITY);
        mainPlayerBody = addPlayerBody(mainPlayer);
    }

    @Override
    public void onStop() {
        super.onStop();
        mainPlayer = null;
        mainPlayerBody = null;
    }

    @Override
    protected void processPlayerInput() {
        // Use keyboard to update forces
        final KeyboardState keyboard = input.getKeyboardState();
        final Vec2 force = mainPlayerBody.m_force;
        force.setZero();
        for (DirectionKey directionKey : DIRECTION_KEYS) {
            // Consume all the key press time
            force.addLocal(directionKey.direction.mul(keyboard.getAndClearPressTime(directionKey.key) / 1e9f * PLAYER_FORCE));
        }
        if (!mainPlayerBody.isAwake()) {
            mainPlayerBody.setAwake(true);
        }
        // Clear any remaining keyboard input
        keyboard.clearAll();
        // Use mouse to update rotation
        final MouseState mouse = input.getMouseState();
        final Vector2f cursorRelative = new Vector2f(mouse.getX() * WIDTH, mouse.getY() * WIDTH).sub(mainPlayer.getPosition());
        Complexf rotation = Complexf.fromRotationTo(Vector2f.UNIT_X, cursorRelative);
        if (cursorRelative.getY() < 0) {
            // This ensures we always use the ccw rotation
            rotation = rotation.invert();
        }
        mainPlayerBody.m_xf.q.c = rotation.getX();
        mainPlayerBody.m_xf.q.s = rotation.getY();
        // Use mouse clicks for bullet firing
        for (int i = mouse.getAndClearPressCount(Button.LEFT); i > 0; i--) {
            spawnBullet(mainPlayer);
        }
        // Clear any remaining mouse input
        mouse.clearAll();
    }

    private static class DirectionKey {
        private final Key key;
        private final Vec2 direction;

        private DirectionKey(Key key, Vec2 direction) {
            this.key = key;
            this.direction = direction;
        }
    }
}
