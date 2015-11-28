package ecse414.fall2015.group21.game.shared.connection;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.codec.TCPEncoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public class TCPConnection implements Connection {
    private Address local;
    private final Address remote;

    public TCPConnection(Address local, Address remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public Address getRemote() {
        return remote;
    }

    @Override
    public Address getLocal() {
        return local;
    }

    @Override
    public void setLocal(Address local) {
        this.local = local;
    }

    @Override
    public void send(Queue<? extends Message> queue) {
        if (queue.isEmpty()) {
            return;
        }
        final Queue<Packet.TCP> encoded = new LinkedList<>();
        queue.forEach(message -> TCPEncoder.INSTANCE.encode(message, local, remote, encoded));
        // TODO: send encoded!
    }

    @Override
    public void receive(Queue<? super Message> queue) {

        // TODO: read ByteBufs from channel, use Packet.TCP.FACTORY.newInstance create and add to this queue
        final Queue<Packet.TCP> received = new LinkedList<>();
        for (Packet.TCP packet : received) {
            TCPDecoder.INSTANCE.decode(packet, remote, queue);
        }
    }

    @Override
    public void close() {
        // TODO: close connection if still open
    }
}
