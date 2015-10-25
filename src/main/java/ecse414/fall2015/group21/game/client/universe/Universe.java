package ecse414.fall2015.group21.game.client.universe;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.util.TickingElement;

import com.flowpowered.math.vector.Vector3f;

/**
 * The game physics, holds all the game state.
 */
public class Universe extends TickingElement {
    private static final Vector3f[] DIRECTIONS = new Vector3f[4];
    private static final float THRUST_FORCE = 7500;
    private final Client game;
    private final Player mainPlayer = new Player(0);
    private final Set<Player> allPlayers = new HashSet<>();
    private volatile Set<Player> playerSnapshots;

    static {
        DIRECTIONS[Key.UP.ordinal()] = Vector3f.UNIT_Y;
        DIRECTIONS[Key.DOWN.ordinal()] = Vector3f.UNIT_Y.negate();
        DIRECTIONS[Key.LEFT.ordinal()] = Vector3f.UNIT_X.negate();
        DIRECTIONS[Key.RIGHT.ordinal()] = Vector3f.UNIT_X;
    }

    public Universe(Client game) {
        super("World", 60);
        this.game = game;
    }

    @Override
    public void onStart() {
        allPlayers.add(mainPlayer);
    }

    @Override
    public void onTick(long dt) {
        updateMainPlayer(dt);
        playerSnapshots = allPlayers.stream().map(Player::snapshot).collect(Collectors.toSet());
    }

    private void updateMainPlayer(long dt) {
        final float dtSec = dt / 1e9f;
        // Update position from speed
        Vector3f position = mainPlayer.getPosition();
        position = position.add(mainPlayer.getSpeed().mul(dtSec));
        mainPlayer.setPosition(position);
        // Update speed from acceleration
        Vector3f speed = mainPlayer.getSpeed();
        speed = speed.add(mainPlayer.getAcceleration().mul(dtSec));
        mainPlayer.setSpeed(speed);
        // Compute the new acceleration from the input
        final Input input = game.getInput();
        final KeyboardState keyboard = input.getKeyboardState();
        Vector3f acceleration = Vector3f.ZERO;
        for (Key key : Key.values()) {
            // Consume all the key press time
            acceleration = acceleration.add(DIRECTIONS[key.ordinal()].mul(keyboard.getAndClearPressTime(key) / 1e9f * THRUST_FORCE));
        }
        mainPlayer.setAcceleration(acceleration);
        // Clear any remaining input
        keyboard.clearAll();
    }

    @Override
    public void onStop() {
        allPlayers.clear();
    }

    public Set<Player> getPlayers() {
        return playerSnapshots;
    }
}
