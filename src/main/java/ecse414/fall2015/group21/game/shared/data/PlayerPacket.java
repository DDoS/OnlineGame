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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * PlayerPacket contains player game information such as position, rotation and health.
 * There are implementations for both UDP and TCP Packets.
 */
public abstract class PlayerPacket implements Packet {
    public final Type type;
    public final long time;
    public final float x, y;
    public final float c, s;
    public final short playerNumber;
    public final short health;

    protected PlayerPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != Type.PLAYER_STATE.id && id != Type.PLAYER_SHOOT.id && id != Type.PLAYER_HEALTH.id) {
            throw new IllegalArgumentException("Not a player packet: " + id);
        }
        type = Type.BY_ID[id];
        time = buf.readLong();
        x = buf.readFloat();
        y = buf.readFloat();
        c = buf.readFloat();
        s = buf.readFloat();
        playerNumber = buf.readShort();
        health = buf.readShort();
    }

    protected PlayerPacket(Type type, long time, float x, float y, float c, float s, short playerNumber, short health) {
        if (type != Type.PLAYER_STATE && type != Type.PLAYER_SHOOT && type != Type.PLAYER_HEALTH) {
            throw new IllegalArgumentException("Not a player packet: " + type.name());
        }
        this.type = type;
        this.time = time;
        this.x = x;
        this.y = y;
        this.c = c;
        this.s = s;
        this.playerNumber = playerNumber;
        this.health = health;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id)
                .writeLong(time)
                .writeFloat(x).writeFloat(y)
                .writeFloat(c).writeFloat(s)
                .writeShort(playerNumber)
                .writeShort(health);
    }

    public static class UDP extends PlayerPacket implements Packet.UDP {
        public final int sharedSecret;

        public UDP(ByteBuf buf) {
            super(buf);
            this.sharedSecret = buf.readInt();
        }

        public UDP(Type type, long time, float x, float y, float c, float s, short playerNumber, short health, int sharedSecret) {
            super(type, time, x, y, c, s, playerNumber, health);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(getType().udpLength)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends PlayerPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(Type type, long time, float x, float y, float c, float s, short playerNumber, short health) {
            super(type, time, x, y, c, s, playerNumber, health);
        }
    }
}
