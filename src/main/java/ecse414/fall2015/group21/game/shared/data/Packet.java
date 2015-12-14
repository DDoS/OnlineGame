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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

/**
 * Represents a packet to be converted to a byte buffer and sent over the network. Exists in two flavours: UDP and TCP
 */
public interface Packet {
    /**
     * Returns the packet type.
     *
     * @return The type
     */
    Type getType();

    /**
     * Stores the packet information in a new byte buffer for sending over the network.
     *
     * @return The packet as a buffer
     */
    ByteBuf asRaw();

    /**
     * A UDP packet.
     */
    interface UDP extends Packet {
    }

    /**
     * A TCP packet.
     */
    interface TCP extends Packet {
    }

    /**
     * All the packet types.
     */
    enum Type {
        CONNECT_REQUEST(0, ConnectRequestPacket.UDP.class, ConnectRequestPacket.TCP.class),
        CONNECT_FULFILL(1, ConnectFulfillPacket.UDP.class, ConnectFulfillPacket.TCP.class),
        TIME_REQUEST(2, TimeRequestPacket.UDP.class, TimeRequestPacket.TCP.class),
        TIME_FULFILL(3, TimeFulfillPacket.UDP.class, TimeFulfillPacket.TCP.class),
        PLAYER_STATE(4, PlayerPacket.UDP.class, PlayerPacket.TCP.class),
        PLAYER_SHOOT(5, PlayerPacket.UDP.class, PlayerPacket.TCP.class),
        PLAYER_HEALTH(6, PlayerPacket.UDP.class, PlayerPacket.TCP.class);
        /**
         * Converts from an ID to a packet type.
         */
        public static final Type[] BY_ID = values();
        /**
         * A factory for converting from byte buffers to UDP packets.
         */
        public static final Factory<Packet.UDP> UDP_FACTORY = new Factory<>();
        /**
         * A factory for converting from byte buffers to TCP packets.
         */
        public static final Factory<Packet.TCP> TCP_FACTORY = new Factory<>();
        /**
         * Gets the packet type ID.
         */
        public final byte id;
        // Implementation classes for each protocol
        private final Class<? extends Packet.UDP> udpImpl;
        private final Class<? extends Packet.TCP> tcpImpl;
        // Sizes for each protocol
        public final int udpLength;
        public final int tcpLength;
        // Size not including protocol dependent data
        public final int baseLength;

        static {
            for (Type type : BY_ID) {
                UDP_FACTORY.register(type.udpImpl, type);
                TCP_FACTORY.register(type.tcpImpl, type);
            }
        }

        Type(int id, Class<? extends Packet.UDP> udpImpl, Class<? extends Packet.TCP> tcpImpl) {
            this.id = (byte) id;
            this.udpImpl = udpImpl;
            this.tcpImpl = tcpImpl;
            // Should be the same parent for both implementations
            baseLength = findLength(udpImpl.getSuperclass());
            // Add one for type byte
            udpLength = findLength(udpImpl) + baseLength + 1;
            tcpLength = findLength(tcpImpl) + baseLength + 1;
        }

        private static int findLength(Class<?> packetClass) {
            int length = 0;
            // Get the declared fields, which excludes the inherited ones
            for (Field field : packetClass.getDeclaredFields()) {
                // Must be public and final only
                final int modifiers = field.getModifiers();
                if (!Modifier.isFinal(modifiers) || !Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
                    continue;
                }
                final Class<?> type = field.getType();
                // Only count primitive types
                if (!type.isPrimitive()) {
                    continue;
                }
                // Sum the lengths
                length += getTypeLength(type);
            }
            // Check the super class which might also be a packet
            return length;
        }

        private static int getTypeLength(Class<?> type) {
            if (type == boolean.class) {
                return 1;
            }
            if (type == byte.class) {
                return 1;
            }
            if (type == short.class) {
                return 2;
            }
            if (type == char.class) {
                return 2;
            }
            if (type == int.class) {
                return 4;
            }
            if (type == long.class) {
                return 8;
            }
            if (type == float.class) {
                return 4;
            }
            if (type == double.class) {
                return 8;
            }
            throw new IllegalArgumentException(type.toString());
        }

        public static Type fromMessageType(Message.Type messageType) {
            switch (messageType) {
                case CONNECT_REQUEST:
                    return Type.CONNECT_REQUEST;
                case CONNECT_FULFILL:
                    return Type.CONNECT_FULFILL;
                case TIME_REQUEST:
                    return Type.TIME_REQUEST;
                case TIME_FULFILL:
                    return Type.TIME_FULFILL;
                case PLAYER_STATE:
                    return Type.PLAYER_STATE;
                case PLAYER_SHOOT:
                    return Type.PLAYER_SHOOT;
                case PLAYER_HEALTH:
                    return Type.PLAYER_HEALTH;
                default:
                    throw new IllegalArgumentException("Not a message type: " + messageType.name());
            }
        }
    }

    /**
     * A factory that uses reflection to call the proper packet constructor for the given byte buffer. The first byte of the packet is used as the ID of the type. Packets are registered by type.
     *
     * @param <T> The type of packet
     */
    class Factory<T extends Packet> {
        private final Map<Byte, Constructor<? extends T>> constructors = new HashMap<>();

        /**
         * Registers a packet class for the given types.
         *
         * @param packet The packet class to use for the types
         * @param type The type the packet class represents
         */
        public void register(Class<? extends T> packet, Packet.Type type) {
            try {
                final Constructor<? extends T> constructor = packet.getDeclaredConstructor(ByteBuf.class);
                constructor.setAccessible(true);
                constructors.put(type.id, constructor);
            } catch (NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            }
        }

        /**
         * Gets the first byte of the buffer, converts it to a packet type and looks for a constructor for that type. On success, constructs a new packet from the buffer.
         *
         * @param buf The buffer to construct the packet from
         * @param <I> The expected type of packet that will be constructed
         * @return The resulting packet
         */
        @SuppressWarnings("unchecked")
        public <I extends T> I newInstance(ByteBuf buf) {
            try {
                final byte typeID = buf.getByte(0);
                final Constructor<? extends T> constructor = constructors.get(typeID);
                if (constructor == null) {
                    throw new InstantiationException("No constructor for packet type " + typeID);
                }
                return (I) constructor.newInstance(buf);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
