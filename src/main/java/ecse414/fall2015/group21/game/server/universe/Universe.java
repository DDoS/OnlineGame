package ecse414.fall2015.group21.game.server.universe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
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
    private static final FixtureDef PLAYER_COLLIDER = new FixtureDef();
    private static final FixtureDef BULLET_COLLIDER = new FixtureDef();
    private World world;
    protected final Map<Player, Body> playerBodies = new HashMap<>();
    protected final Map<Bullet, Body> bulletBodies = new HashMap<>();
    protected volatile long accumulatedTime;
    protected volatile long seed = System.nanoTime();
    private volatile Map<Integer, Player> playerSnapshots = Collections.emptyMap();
    private volatile Set<Bullet> bulletSnapshots = Collections.emptySet();
    private final Queue<Message> networkMessages = new ConcurrentLinkedQueue<>();
    protected final Queue<Message> events = new ConcurrentLinkedQueue<>();

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
        PLAYER_COLLIDER.filter.groupIndex = 1;

        final CircleShape bulletShape = new CircleShape();
        bulletShape.setRadius(BULLET_RADIUS);
        BULLET_COLLIDER.shape = bulletShape;
        BULLET_COLLIDER.density = 1;
        BULLET_COLLIDER.isSensor = true;
        BULLET_COLLIDER.filter.groupIndex = 2;
    }

    public Universe() {
        super("Universe", 60);
    }

    @Override
    public void onStart() {
        // Reset the game time
        accumulatedTime = 0;
        // Create world
        world = new World(new Vec2(0, 0));
        world.setContactFilter(new CustomContactFilter());
        // Create screen border
        final ChainShape border = new ChainShape();
        border.createLoop(new Vec2[]{
                new Vec2(0, 0),
                new Vec2(WIDTH, 0),
                new Vec2(WIDTH, HEIGHT),
                new Vec2(0, HEIGHT)
        }, 4);
        final BodyDef def = new BodyDef();
        def.type = BodyType.STATIC;
        final Body body = world.createBody(def);
        body.createFixture(border, 1).m_filter.groupIndex = 0;
    }

    @Override
    public void onTick(long dt) {
        accumulatedTime += dt / 1000;
        processExternalInput();
        updateRotations(playerBodies);
        world.step(dt / 1e9f, 10, 8);
        processBullets();
        updateTimePositions(playerBodies);
        updateTimePositions(bulletBodies);
        playerSnapshots = createSnapshots(playerBodies).collect(Collectors.toMap(Player::getNumber, player -> player));
        bulletSnapshots = createSnapshots(bulletBodies).collect(Collectors.toSet());
    }

    private <T extends Positioned> void updateTimePositions(Map<T, Body> originals) {
        originals.forEach((player, body) -> player.setTimePosition(accumulatedTime, new Vector2f(body.m_xf.p.x, body.m_xf.p.y)));
    }

    private <T extends Positioned> void updateRotations(Map<T, Body> originals) {
        originals.forEach((player, body) -> player.setRotation(new Complexf(body.m_xf.q.c, body.m_xf.q.s)));
    }

    private <T extends Snapshotable<T>> Stream<T> createSnapshots(Map<T, Body> originals) {
        return originals.keySet().stream().map(T::snapshot);
    }

    protected void processExternalInput() {
        while (!networkMessages.isEmpty()) {
            final Message message = networkMessages.poll();
            switch (message.getType()) {
                case CONNECT_FULFILL:
                    processConnectFulfillMessage((ConnectFulfillMessage) message);
                    break;
                case PLAYER_STATE:
                case PLAYER_SHOOT:
                case PLAYER_HEALTH:
                    processPlayerMessage((PlayerMessage) message);
                    break;
                default:
                    // Not a message we should care about
                    break;
            }
        }
    }

    protected void processConnectFulfillMessage(ConnectFulfillMessage message) {
        if (playerFromNumber(message.playerNumber) != null) {
            throw new IllegalStateException("Player " + message.playerNumber + " is already connected");
        }
        addPlayerBody(new Player(message.playerNumber, message.time, Vector2f.ONE, Complexf.IDENTITY), false);
        System.out.println("Spawned player " + message.playerNumber);
    }

    protected void processPlayerMessage(PlayerMessage message) {
        switch (message.getType()) {
            case PLAYER_STATE: {
                final Player player = playerFromNumber(message.playerNumber);
                if (player != null) {
                    // Update remote player if it exists
                    final Body body = playerBodies.get(player);
                    body.m_xf.p.x = message.position.getX();
                    body.m_xf.p.y = message.position.getY();
                    body.m_xf.q.c = message.rotation.getX();
                    body.m_xf.q.s = message.rotation.getY();
                }
                break;
            }
            case PLAYER_SHOOT: {
                spawnBullet(message.time, message.position, message.rotation, message.playerNumber);
                // Broadcast the bullet to all clients
                events.add(message);
                break;
            }
            case PLAYER_HEALTH: {
                if (message.health <= 0) {
                    // Kill player
                    System.out.println("Killed player " + message.playerNumber);
                    removePlayerBody(playerFromNumber(message.playerNumber));
                    // Broadcast the death to all clients
                    events.add(message);
                }
                break;
            }
        }
    }

    protected Body addPlayerBody(Player player, boolean dynamic) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = dynamic ? BodyType.DYNAMIC : BodyType.STATIC;
        bodyDef.position.set(player.getPosition().getX(), player.getPosition().getY());
        bodyDef.fixedRotation = true;
        final Body body = world.createBody(bodyDef);
        body.createFixture(PLAYER_COLLIDER);
        body.m_userData = player.getNumber();
        playerBodies.put(player, body);
        return body;
    }

    protected void removePlayerBody(Player player) {
        final Body body = playerBodies.remove(player);
        if (body != null) {
            world.destroyBody(body);
        }
    }

    protected void spawnBullet(long shotTime, Vector2f position, Complexf rotation, int playerNumber) {
        final BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(position.getX(), position.getY());
        bodyDef.linearVelocity = new Vec2(rotation.getX(), rotation.getY()).mulLocal(BULLET_SPEED);
        bodyDef.position.addLocal(rotation.getX() * PLAYER_RADIUS + BULLET_RADIUS, rotation.getY() * PLAYER_RADIUS + BULLET_RADIUS)
                .addLocal(bodyDef.linearVelocity.mul((accumulatedTime - shotTime) / 1e6f));
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;
        final Body body = world.createBody(bodyDef);
        body.createFixture(BULLET_COLLIDER);
        body.m_userData = playerNumber;
        bulletBodies.put(new Bullet(playerNumber, accumulatedTime, Vector2f.ZERO, rotation), body);
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
                    System.out.println("player " + bullet.getNumber() + " killed player " + contactList.other.m_userData);
                    final Player player = playerFromNumber((int) contactList.other.m_userData);
                    removePlayerBody(player);
                    // No need to tell the clients, they track the bullet on their side
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

    protected Player playerFromNumber(int playerNumber) {
        for (Player player : playerBodies.keySet()) {
            if (player.getNumber() == playerNumber) {
                return player;
            }
        }
        return null;
    }

    @Override
    public void onStop() {
        playerBodies.clear();
        bulletBodies.clear();
        world = null;
        playerSnapshots = Collections.emptyMap();
        bulletSnapshots = Collections.emptySet();
    }

    public Map<Integer, Player> getPlayers() {
        return playerSnapshots;
    }

    public Set<Bullet> getBullets() {
        return bulletSnapshots;
    }

    public long getSeed() {
        return seed;
    }

    public long getTime() {
        return accumulatedTime;
    }

    public void handOff(Message message) {
        networkMessages.add(message);
    }

    public Queue<Message> getEvents() {
        return events;
    }

    private static boolean outOfBounds(Vector2f position, float radius) {
        return position.getX() < -radius || position.getX() > WIDTH + radius || position.getY() < -radius || position.getY() > HEIGHT + radius;
    }

    private static class CustomContactFilter extends ContactFilter {
        @Override
        public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
            // Bullets don't collide with each other
            return !(fixtureA.m_filter.groupIndex == 2 && fixtureB.m_filter.groupIndex == 2)
                    // Players don't collide with their bullets
                    && !(fixtureA.m_body.m_userData != null && fixtureA.m_body.m_userData.equals(fixtureB.m_body.m_userData))
                    // Bullets don't collide with the border
                    && !(fixtureA.m_filter.groupIndex == 2 && fixtureB.m_filter.groupIndex == 0)
                    && !(fixtureA.m_filter.groupIndex == 0 && fixtureB.m_filter.groupIndex == 2);
        }
    }
}

