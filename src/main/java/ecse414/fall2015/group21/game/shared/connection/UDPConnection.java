package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.UDPDecoder;
import ecse414.fall2015.group21.game.shared.codec.UDPEncoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

/**
 *
 */
public class UDPConnection implements Connection {
    private final Address local;
    private final Address remote;
    private final Queue<Packet.UDP> received = new LinkedList<>();
    private final Channel ch;

    public UDPConnection(Address local, Address remote, Channel ch) {
        this.local = local;
        this.remote = remote;
        this.ch = ch;
    }

    @Override
    public Address getAddress() {
        return remote;
    }

    @Override
    public void send(Queue<? extends Message> queue) {
        final Queue<Packet.UDP> encoded = new LinkedList<>();
        queue.forEach(message -> UDPEncoder.INSTANCE.encode(message, local, encoded));
        // Send each packet
        for(Packet.UDP packet : encoded) {
            try {
                ch.writeAndFlush(
                        new DatagramPacket(
                                packet.asRaw(),
                                new InetSocketAddress(this.remote.asInetAddress(), this.remote.getPort()
                                ))).sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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

    void handOff(Packet.UDP received) {
        this.received.add(received);
    }
}
