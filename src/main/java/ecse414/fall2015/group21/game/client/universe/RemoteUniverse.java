/*
 * This file is part of Online Game, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2015 Group 21
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ecse414.fall2015.group21.game.client.universe;

import ecse414.fall2015.group21.game.client.input.Button;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.Key;
import ecse414.fall2015.group21.game.client.input.KeyboardState;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
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
    private volatile int userPlayerNumber = -1;

    public RemoteUniverse(Input input) {
        this.input = input;
    }

    @Override
    public void onStop() {
        super.onStop();
        userPlayer = null;
        userPlayerBody = null;
        userPlayerNumber = -1;
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
        final Vector2f position = new Vector2f(userPlayerBody.m_xf.p.x, userPlayerBody.m_xf.p.y);
        for (int i = mouse.getAndClearPressCount(Button.LEFT); i > 0; i--) {
            spawnBullet(accumulatedTime, position, rotation, userPlayerNumber);
            events.add(new PlayerMessage(Message.Type.PLAYER_SHOOT, accumulatedTime, position, rotation, (short) 1, userPlayerNumber));
        }
        // Clear any remaining mouse input
        mouse.clearAll();
    }

    @Override
    protected void processConnectFulfillMessage(ConnectFulfillMessage message) {
        // Check if not already logged in
        if (userPlayer != null) {
            throw new IllegalStateException("Logged into the server before logout");
        }
        // Update time and seed
        seed = message.seed;
        accumulatedTime = message.time;
        // Don't add user yet, wait for first state message
        userPlayerNumber = message.playerNumber;
    }

    @Override
    protected void processPlayerMessage(PlayerMessage message) {
        switch (message.getType()) {
            case PLAYER_STATE: {
                Player player = playerFromNumber(message.playerNumber);
                final Body body;
                if (player == null) {
                    // Add the player if missing
                    player = new Player(message.playerNumber, message.time, message.position, message.rotation);
                    if (message.playerNumber == userPlayerNumber) {
                        // This is the user player, create it but don't update it form messages
                        userPlayerBody = body = addPlayerBody(userPlayer = player, true);
                    } else {
                        // Else it's a remote player
                        body = addPlayerBody(player, false);
                    }
                    System.out.println("Spawned player " + message.playerNumber);
                } else {
                    body = playerBodies.get(player);
                }
                if (message.playerNumber != userPlayerNumber) {
                    // Update remote player
                    body.m_xf.p.x = message.position.getX();
                    body.m_xf.p.y = message.position.getY();
                    body.m_xf.q.c = message.rotation.getX();
                    body.m_xf.q.s = message.rotation.getY();
                }
                break;
            }
            case PLAYER_SHOOT: {
                if (message.playerNumber != userPlayerNumber) {
                    // Don't re-shoot the user's bullet
                    spawnBullet(message.time, message.position, message.rotation, message.playerNumber);
                }
                break;
            }
            case PLAYER_HEALTH:
                if (message.health <= 0) {
                    removePlayerBody(playerFromNumber(message.playerNumber));
                    // Check if the death is the user player
                    if (message.playerNumber == userPlayerNumber) {
                        userPlayer = null;
                        userPlayerBody = null;
                    }
                }
                break;
        }
    }

    public int getUserPlayerNumber() {
        return userPlayerNumber;
    }

    public Player getUserPlayer() {
        // Return the snapshot, not the live version!
        return userPlayerNumber < 0 ? null : getPlayers().get(userPlayerNumber);
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
