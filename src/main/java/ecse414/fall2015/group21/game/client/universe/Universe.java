package ecse414.fall2015.group21.game.client.universe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.input.Button;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.util.TickingElement;
import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactEdge;

import com.flowpowered.math.TrigMath;
import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * The game physics, holds all the game state.
 */
public class Universe extends TickingElement {
    public static final int WIDTH = 16, HEIGHT = 9;
    public static final float PLAYER_RADIUS = 0.5f;
    public static final float PLAYER_FORCE = 93.75f;
    public static final float BULLET_RADIUS = 0.05f;
    public static final float BULLET_SPEED = 8;
    private static final DirectionKey[] DIRECTION_KEYS = {
            new DirectionKey(Key.DOWN, new Vec2(0, -1)),
            new DirectionKey(Key.UP, new Vec2(0, 1)),
            new DirectionKey(Key.LEFT, new Vec2(-1, 0)),
            new DirectionKey(Key.RIGHT, new Vec2(1, 0))
    };
    private static final FixtureDef PLAYER_COLLIDER = new FixtureDef();
    private static final FixtureDef BULLET_COLLIDER = new FixtureDef();
    private final Client game;
    private World world;
    private Player mainPlayer;
    private Body mainPlayerBody;
    private final Map<Player, Body> playerBodies = new HashMap<>();
    private final Map<Bullet, Body> bulletBodies = new HashMap<>();
    private volatile Set<Player> playerSnapshots = new HashSet<>();
    private volatile Set<Bullet> bulletSnapshots = new HashSet<>();
    private volatile long seed = System.nanoTime();

    static {
        final PolygonShape playerShape = new PolygonShape();
        final float edgeCoordinate45Deg = (float) TrigMath.HALF_SQRT_OF_TWO * Universe.PLAYER_RADIUS;
        playerShape.set(new Vec2[]{
                new Vec2(-edgeCoordinate45Deg, edgeCoordinate45Deg),
                new Vec2(-edgeCoordinate45Deg, -edgeCoordinate45Deg),
                new Vec2(Universe.PLAYER_RADIUS, 0)
        }, 3);
        PLAYER_COLLIDER.shape = playerShape;
        PLAYER_COLLIDER.density = 1;
        PLAYER_COLLIDER.restitution = 0.2f;
        PLAYER_COLLIDER.filter.groupIndex = 0;

        final CircleShape bulletShape = new CircleShape();
        bulletShape.setRadius(BULLET_RADIUS);
        BULLET_COLLIDER.shape = bulletShape;
        BULLET_COLLIDER.density = 1;
        BULLET_COLLIDER.isSensor = true;
        BULLET_COLLIDER.filter.groupIndex = 1;
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
        world.setContactFilter(new CustomContactFilter());
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
        processBullets();
        world.step(dt / 1e9f, 10, 8);
        updatePositions(playerBodies);
        updatePositions(bulletBodies);
        playerSnapshots = createSnapshots(playerBodies);
        bulletSnapshots = createSnapshots(bulletBodies);
    }

    private <T extends Positioned> void updatePositions(Map<T, Body> originals) {
        originals.forEach((player, body) -> player.setPosition(new Vector2f(body.m_xf.p.x, body.m_xf.p.y)));
    }

    private <T extends Snapshotable<T>> Set<T> createSnapshots(Map<T, Body> originals) {
        return originals.keySet().stream().map(T::snapshot).collect(Collectors.toSet());
    }

    private Body addPlayerBody(Player player) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(player.getPosition().getX(), player.getPosition().getY());
        bodyDef.fixedRotation = true;
        final Body body = world.createBody(bodyDef);
        body.createFixture(PLAYER_COLLIDER);
        body.m_userData = player.getNumber();
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
        mainPlayer.setRotation(rotation);
        mainPlayerBody.m_xf.q.c = rotation.getX();
        mainPlayerBody.m_xf.q.s = rotation.getY();
        // Use mouse clicks for bullet firing
        for (int i = mouse.getAndClearPressCount(Button.LEFT); i > 0; i--) {
            spawnBullet(mainPlayer);
        }
        // Clear any remaining mouse input
        mouse.clearAll();
    }

    private void spawnBullet(Player player) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KINEMATIC;
        final Body playerBody = playerBodies.get(player);
        final Complexf rotation = player.getRotation();
        bodyDef.position.set(playerBody.m_xf.p);
        bodyDef.position.addLocal(rotation.getX() * PLAYER_RADIUS + BULLET_RADIUS, rotation.getY() * PLAYER_RADIUS + BULLET_RADIUS);
        bodyDef.linearVelocity = new Vec2(rotation.getX(), rotation.getY()).mulLocal(BULLET_SPEED).addLocal(playerBody.m_linearVelocity);
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;
        final Body body = world.createBody(bodyDef);
        body.createFixture(BULLET_COLLIDER);
        final int number = player.getNumber();
        body.m_userData = number;
        bulletBodies.put(new Bullet(number), body);
    }

    private void processBullets() {
        for (Iterator<Entry<Bullet, Body>> iterator = bulletBodies.entrySet().iterator(); iterator.hasNext(); ) {
            final Entry<Bullet, Body> entry = iterator.next();
            final Bullet bullet = entry.getKey();
            final Body body = entry.getValue();
            boolean remove = false;
            if (outOfBounds(bullet.getPosition(), BULLET_RADIUS)) {
                remove = true;
            } else {
                ContactEdge contactList = body.getContactList();
                while (contactList != null && contactList.contact.isTouching()) {
                    System.out.println("Hit player " + contactList.other.m_userData);
                    remove = true;
                    contactList = contactList.next;
                }
            }
            if (remove) {
                world.destroyBody(body);
                iterator.remove();
            }
        }
    }

    private boolean outOfBounds(Vector2f position, float radius) {
        return position.getX() < -radius || position.getX() > WIDTH + radius || position.getY() < -radius || position.getY() > HEIGHT + radius;
    }

    @Override
    public void onStop() {
        playerBodies.clear();
        mainPlayer = null;
        world = null;
    }

    public Set<Player> getPlayers() {
        return playerSnapshots;
    }

    public Set<Bullet> getBullets() {
        return bulletSnapshots;
    }

    public long getSeed() {
        return seed;
    }

    private static class DirectionKey {
        private final Key key;
        private final Vec2 direction;

        private DirectionKey(Key key, Vec2 direction) {
            this.key = key;
            this.direction = direction;
        }
    }

    private static class CustomContactFilter extends ContactFilter {
        @Override
        public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
            // Bullets don't collide with each other
            return !(fixtureA.getFilterData().groupIndex == 1 && fixtureB.getFilterData().groupIndex == 1)
                    // Players don't collide with their bullets
                    && !(fixtureA.getBody().m_userData != null && fixtureA.getBody().m_userData.equals(fixtureB.getBody().m_userData));
        }
    }
}
