package ecse414.fall2015.group21.game.shared.connection;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.UDPDecoder;
import ecse414.fall2015.group21.game.shared.codec.UDPEncoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public class UDPConnection implements Connection {
    private final Address local;
    private final Address remote;
    private final Queue<Packet.UDP> received = new LinkedList<>();

    public UDPConnection(Address local, Address remote) {
        this.local = local;
        this.remote = remote;
    }

    @Override
    public Address getAddress() {
        return remote;
    }

    @Override
    public void send(Queue<? extends Message> queue) {
        final Queue<Packet.UDP> encoded = new LinkedList<>();
        queue.forEach(message -> UDPEncoder.INSTANCE.encode(message, local, encoded));
        // TODO: send encoded!
    }

    @Override
    public void receive(Queue<? super Message> queue) {
        for (Packet.UDP packet : received) {
            UDPDecoder.INSTANCE.decode(packet, remote, queue);
        }
    }

    @Override
    public void close() {
        // Do nothing since UDP is connectionless
    }

    // Adds a packet to the received buffer
    void buffer(Packet.UDP received) {
        this.received.add(received);
    }
}
