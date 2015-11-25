package ecse414.fall2015.group21.game.client.universe;

import ecse414.fall2015.group21.game.client.input.Button;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
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
    private Player userPlayer = null;
    private Body userPlayerBody = null;

    public RemoteUniverse(Input input) {
        this.input = input;
    }

    @Override
    public void onStop() {
        super.onStop();
        userPlayer = null;
        userPlayerBody = null;
    }

    @Override
    protected void processExternalInput() {
        super.processExternalInput();
        // Check if we're logged in first
        if (userPlayer == null) {
            return;
        }
        // Use keyboard to update forces
        final KeyboardState keyboard = input.getKeyboardState();
        final Vec2 force = userPlayerBody.m_force;
        force.setZero();
        for (DirectionKey directionKey : DIRECTION_KEYS) {
            // Consume all the key press time
            force.addLocal(directionKey.direction.mul(keyboard.getAndClearPressTime(directionKey.key) / 1e9f * PLAYER_FORCE));
        }
        if (!userPlayerBody.isAwake()) {
            userPlayerBody.setAwake(true);
        }
        // Clear any remaining keyboard input
        keyboard.clearAll();
        // Use mouse to update rotation
        final MouseState mouse = input.getMouseState();
        final Vector2f cursorRelative = new Vector2f(mouse.getX() * WIDTH, mouse.getY() * WIDTH).sub(userPlayer.getPosition());
        Complexf rotation = Complexf.fromRotationTo(Vector2f.UNIT_X, cursorRelative);
        if (cursorRelative.getY() < 0) {
            // This ensures we always use the ccw rotation
            rotation = rotation.invert();
        }
        userPlayerBody.m_xf.q.c = rotation.getX();
        userPlayerBody.m_xf.q.s = rotation.getY();
        // Use mouse clicks for bullet firing
        for (int i = mouse.getAndClearPressCount(Button.LEFT); i > 0; i--) {
            spawnBullet(userPlayer);
        }
        // Clear any remaining mouse input
        mouse.clearAll();
    }

    @Override
    protected void processConnectFulfillMessage(ConnectFulfillMessage message) {
        super.processConnectFulfillMessage(message);
        // Add user player
        if (userPlayer != null) {
            throw new IllegalStateException("Logged into the server before logout");
        }
        userPlayer = new Player(message.playerNumber, getTime(), Vector2f.ONE, Complexf.IDENTITY);
        userPlayerBody = addPlayerBody(userPlayer);
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
