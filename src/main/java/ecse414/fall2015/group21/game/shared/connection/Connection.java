package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetAddress;
import java.util.Collection;

import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public interface Connection<T extends Packet> {
    void open(InetAddress address);

    void send(Collection<? extends T> queue);

    void receive(Collection<? super T> queue);

    void close();
}
