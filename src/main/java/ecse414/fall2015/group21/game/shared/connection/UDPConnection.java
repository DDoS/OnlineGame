package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetSocketAddress;
import java.util.Collection;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 *
 */
public class UDPConnection implements Connection<Message> {
    @Override
    public void open(InetSocketAddress address) {

    }

    @Override
    public void send(Collection<? extends Message> queue) {

    }

    @Override
    public void receive(Collection<? super Message> queue) {

    }

    @Override
    public void close() {

    }
}
