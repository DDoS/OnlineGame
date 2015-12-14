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
package ecse414.fall2015.group21.game.shared.data;

import ecse414.fall2015.group21.game.server.universe.Player;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * PlayerMessage contains player game information, such as position in the game, rotation of the player and player health.
 */
public class PlayerMessage implements Message {
    public final Type type;
    public final long time;
    public final Vector2f position;
    public final Complexf rotation;
    public final short health;
    public final int playerNumber;

    public PlayerMessage(Type type, Player player, boolean died) {
        this(type, player.getTime(), player.getPosition(), player.getRotation(), (short) (died ? 0 : 1), player.getNumber());
    }

    public PlayerMessage(Type type, long time, Vector2f position, Complexf rotation, short health, int playerNumber) {
        if (type != Type.PLAYER_STATE && type != Type.PLAYER_SHOOT && type != Type.PLAYER_HEALTH) {
            throw new IllegalArgumentException("Not a player message: " + type.name());
        }
        this.type = type;
        this.time = time;
        this.position = position;
        this.rotation = rotation;
        this.health = health;
        this.playerNumber = playerNumber;
    }

    @Override
    public Type getType() {
        return type;
    }
}
