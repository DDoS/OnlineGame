package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetAddress;
import java.util.Collection;

import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public class UDPConnection implements Connection<Packet.UDP> {
    @Override
    public void open(InetAddress address) {

    }

    @Override
    public void send(Collection<? extends Packet.UDP> queue) {

    }

    @Override
    public void receive(Collection<? super Packet.UDP> queue) {

    }

    @Override
    public void close() {

    }
}
