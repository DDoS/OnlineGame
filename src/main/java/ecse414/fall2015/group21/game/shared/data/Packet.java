package ecse414.fall2015.group21.game.shared.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
        /**
         * A factory for converting from byte buffers to UDP packets.
         */
        Factory<Packet.UDP> FACTORY = new Factory<>();
    }

    /**
     * A TCP packet.
     */
    interface TCP extends Packet {
        /**
         * A factory for converting from byte buffers to TCP packets.
         */
        Factory<Packet.TCP> FACTORY = new Factory<>();
    }

    /**
     * All the packet types.
     */
    enum Type {
        CONNECT_REQUEST(0),
        CONNECT_FULFILL(1),
        TIME_REQUEST(2),
        TIME_FULFILL(3),
        PLAYER_STATE(4),
        PLAYER_SHOOT(5),
        PLAYER_HEALTH(6);
        /**
         * Converts from an ID to a packet type.
         */
        public static final Type[] BY_ID = values();
        /**
         * Gets the packet type ID.
         */
        public final byte id;

        Type(int id) {
            this.id = (byte) id;
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
         * @param types The types the packet class represents
         */
        public void register(Class<? extends T> packet, Packet.Type... types) {
            try {
                final Constructor<? extends T> constructor = packet.getDeclaredConstructor(ByteBuf.class);
                constructor.setAccessible(true);
                for (Type type : types) {
                    constructors.put(type.id, constructor);
                }
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
