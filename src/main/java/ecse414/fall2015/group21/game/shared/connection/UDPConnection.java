package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.UDPDecoder;
import ecse414.fall2015.group21.game.shared.codec.UDPEncoder;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 *
 */
public class UDPConnection implements Connection {
    private Address local;
    private final Address remote;
    private final Queue<Packet.UDP> received = new LinkedList<>();
    private final Channel channel;
    private boolean managed;
    private EventLoopGroup group = null;
    private UDPConnectionHandler handler = null;

    /**
     * Creates a new standalone UDP connection. This connection takes care of reading and sending. No connection manager is needed. This also opens the port.
     *
     * @param local The local address of the connection (port)
     * @param remote The remote address to send to (ip and port)
     */
    public UDPConnection(Address local, Address remote) {
        this.local = local;
        this.remote = remote;
        group = new NioEventLoopGroup();
        handler = new UDPConnectionHandler();
        try {
            channel = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(handler)
                    .bind(local.getPort()).syncUninterruptibly()
                    .channel();
        } catch (Exception exception) {
            group.shutdownGracefully();
            throw new RuntimeException("Failed to create channel at " + local, exception);
        }
        System.out.println("Listening at " + local.toString());
        managed = false;
    }

    /**
     * Creates a UDP connection for an existing channel. This connection won't read the channel, but will use it to send. It is expected that a connection manager will take care of the read task using
     * {@link #handOff(Packet.UDP)}
     *
     * @param local The local address of the connection (port)
     * @param remote The remote address to send to (ip and port)
     * @param channel The channel use for sending
     */
    public UDPConnection(Address local, Address remote, Channel channel) {
        this.local = local;
        this.remote = remote;
        this.channel = channel;
        managed = true;
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
        final Queue<Packet.UDP> encoded = new LinkedList<>();
        queue.forEach(message -> UDPEncoder.INSTANCE.encode(message, local, remote, encoded));
        // Send each packet
        final InetSocketAddress address = remote.asInetSocketAddress();
        for (Packet.UDP packet : encoded) {
            // No need to sync, we don't care about the result, just try to push the packets
            channel.write(new DatagramPacket(packet.asRaw(), address));
        }
        channel.flush();
    }

    @Override
    public void receive(Queue<? super Message> queue) {
        // If we don't have a manager, we take care of reading the packets
        if (!managed) {
            final Queue<DatagramPacket> packets = new LinkedList<>();
            handler.readPackets(packets);
            packets.forEach(packet -> {
                received.add(Packet.UDP.FACTORY.newInstance(packet.content()));
                packet.release();
            });
        }
        // Decode packets and place in given queue
        while (!received.isEmpty()) {
            final Packet.UDP packet = received.poll();
            // Check for a connect fulfill so we can extract the shared secret and update the local address
            if (packet instanceof ConnectFulfillPacket.UDP && !local.hasSharedSecret()) {
                setLocal(local.connectClient(((ConnectFulfillPacket.UDP) packet).sharedSecret));
            }
            UDPDecoder.INSTANCE.decode(packet, remote, queue);
        }
    }

    @Override
    public void close() {
        if (!managed) {
            group.shutdownGracefully();
        }
        // Else the manager is the channel owner, let it close its resources
    }

    void handOff(Packet.UDP received) {
        this.received.add(received);
    }
}
