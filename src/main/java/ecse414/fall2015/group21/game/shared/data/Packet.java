package ecse414.fall2015.group21.game.shared.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

/**
 *
 */
public interface Packet {
    Type getType();

    ByteBuf asRaw();

    interface UDP extends Packet {
        Factory<Packet.UDP> FACTORY = new Factory<>();
    }

    interface TCP extends Packet {
        Factory<Packet.TCP> FACTORY = new Factory<>();
    }

    enum Type {
        CONNECT_REQUEST(0),
        CONNECT_FULFILL(1),
        TIME_SYNC_REQUEST(2),
        TIME_SYNC_FULFILL(3),
        PLAYER_STATE(4),
        PLAYER_SHOOT(5),
        PLAYER_HEALTH(6);
        public static final Type[] BY_ID = values();
        public final byte id;

        Type(int id) {
            this.id = (byte) id;
        }
    }

    class Factory<T extends Packet> {
        private final Map<Byte, Constructor<? extends T>> constructors = new HashMap<>();

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

        @SuppressWarnings("unchecked")
        public <I extends T> I newInstance(ByteBuf buf) {
            try {
                return (I) constructors.get(buf.getByte(0)).newInstance(buf);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
