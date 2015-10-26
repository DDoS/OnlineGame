package ecse414.fall2015.group21.game.client.universe;

import java.util.HashMap;
import java.util.Map;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.util.TickingElement;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * The game physics, holds all the game state.
 */
public class Universe extends TickingElement {
    public static final int WIDTH = 16, HEIGHT = 9;
    private static final DirectionKey[] DIRECTION_KEYS = {
            new DirectionKey(Key.DOWN, new Vec2(0, -1)),
            new DirectionKey(Key.UP, new Vec2(0, 1)),
            new DirectionKey(Key.LEFT, new Vec2(-1, 0)),
            new DirectionKey(Key.RIGHT, new Vec2(1, 0))
    };
    private static final float THRUST_FORCE = 93.75f;
    private static final FixtureDef PLAYER_COLLIDER = new FixtureDef();
    private final Client game;
    private World world;
    private Player mainPlayer;
    private Body mainPlayerBody;
    private final Map<Player, Body> playerBodies = new HashMap<>();
    private volatile TIntObjectMap<Player> playerSnapshots = new TIntObjectHashMap<>();

    static {
        final CircleShape shape = new CircleShape();
        shape.setRadius(0.5f);
        PLAYER_COLLIDER.shape = shape;
        PLAYER_COLLIDER.density = 1;
        PLAYER_COLLIDER.restitution = 0.2f;
    }

    public Universe(Client game) {
        super("World", 60);
        this.game = game;
    }

    @Override
    public void onStart() {
        // Create world and add border
        world = new World(new Vec2(0, 0));
        final ChainShape border = new ChainShape();
        border.createLoop(new Vec2[]{new Vec2(0, 0), new Vec2(WIDTH, 0), new Vec2(WIDTH, HEIGHT), new Vec2(0, HEIGHT)}, 4);
        final BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        final Body body = world.createBody(def);
        body.createFixture(border, 1);
        // Add client player
        mainPlayer = new Player(0);
        mainPlayer.setPosition(Vector2f.ONE);
        mainPlayerBody = addPlayerBody(mainPlayer);
        // Add a test player
        final Player test = new Player(1);
        test.setPosition(new Vector2f(3, 7));
        addPlayerBody(test);
    }

    @Override
    public void onTick(long dt) {
        processPlayerInput();
        world.step(dt / 1e9f, 10, 8);
        updatePlayerPositions();
        final TIntObjectMap<Player> playerSnapshots = new TIntObjectHashMap<>();
        playerBodies.keySet().stream().map(Player::snapshot).iterator().forEachRemaining(player -> playerSnapshots.put(player.getNumber(), player));
        this.playerSnapshots = playerSnapshots;
    }

    private Body addPlayerBody(Player player) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(player.getPosition().getX(), player.getPosition().getY());
        bodyDef.fixedRotation = true;
        final Body body = world.createBody(bodyDef);
        body.createFixture(PLAYER_COLLIDER);
        playerBodies.put(player, body);
        return body;
    }

    private void processPlayerInput() {
        final Input input = game.getInput();
        // Use keyboard to update forces
        final KeyboardState keyboard = input.getKeyboardState();
        final Vec2 force = mainPlayerBody.m_force;
        force.setZero();
        for (DirectionKey directionKey : DIRECTION_KEYS) {
            // Consume all the key press time
            force.addLocal(directionKey.direction.mul(keyboard.getAndClearPressTime(directionKey.key) / 1e9f * THRUST_FORCE));
        }
        if (!mainPlayerBody.isAwake()) {
            mainPlayerBody.setAwake(true);
        }
        // Clear any remaining keyboard input
        keyboard.clearAll();
        // Use mouse to update turret rotation
        final MouseState mouse = input.getMouseState();
        final Vector2f cursorRelative = new Vector2f(mouse.getX() * WIDTH, mouse.getY() * WIDTH).sub(mainPlayer.getPosition());
        Complexf rotation = Complexf.fromRotationTo(Vector2f.UNIT_X, cursorRelative);
        if (cursorRelative.getY() < 0) {
            // This ensures we always use the ccw rotation
            rotation = rotation.invert();
        }
        mainPlayer.setRotation(rotation);
        // Clear any remaining mouse input
        mouse.clearAll();
    }

    private void updatePlayerPositions() {
        playerBodies.forEach((player, body) -> player.setPosition(new Vector2f(body.m_xf.p.x, body.m_xf.p.y)));
    }

    @Override
    public void onStop() {
        playerBodies.clear();
        mainPlayer = null;
        world = null;
    }

    public TIntObjectMap<Player> getPlayers() {
        return playerSnapshots;
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
