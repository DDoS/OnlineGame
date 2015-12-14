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
 * ConnectFulfillPacket is a Packet type used to respond to a connection request from a client.
 * There are implementations for both UDP and TCP Packets.
 */
public abstract class ConnectFulfillPacket implements Packet {
    public final short playerNumber;
    public final long seed;
    public final long time;

    protected ConnectFulfillPacket(ByteBuf buf) {
        final byte id = buf.readByte();
        if (id != getType().id) {
            throw new IllegalArgumentException("Invalid packet ID: " + id);
        }
        playerNumber = buf.readShort();
        seed = buf.readLong();
        time = buf.readLong();
    }

    protected ConnectFulfillPacket(short playerNumber, long seed, long time) {
        this.playerNumber = playerNumber;
        this.seed = seed;
        this.time = time;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_FULFILL;
    }

    @Override
    public ByteBuf asRaw() {
        return Unpooled.directBuffer(getType().baseLength)
                .writeByte(getType().id)
                .writeShort(playerNumber)
                .writeLong(seed)
                .writeLong(time);
    }

    public static class UDP extends ConnectFulfillPacket implements Packet.UDP {
        public final int sharedSecret;

        public UDP(ByteBuf buf) {
            super(buf);
            sharedSecret = buf.readInt();
        }

        public UDP(short playerNumber, long seed, long time, int sharedSecret) {
            super(playerNumber, seed, time);
            this.sharedSecret = sharedSecret;
        }

        @Override
        public ByteBuf asRaw() {
            final ByteBuf buf = super.asRaw();
            return buf.capacity(getType().udpLength)
                    .writeInt(sharedSecret);
        }
    }

    public static class TCP extends ConnectFulfillPacket implements Packet.TCP {
        public TCP(ByteBuf buf) {
            super(buf);
        }

        public TCP(short playerNumber, long seed, long time) {
            super(playerNumber, seed, time);
        }
    }
}
