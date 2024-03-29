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
 * Represents a UDP connection. Holds the Address of the local and remote sides of the connection.
 * Responsible for sending and receiving UDP packets.
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
        if (queue.isEmpty()) {
            return;
        }
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
                received.add(Packet.Type.UDP_FACTORY.newInstance(packet.content()));
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

    /**
     * Passes a received packet to the connection for it to be handled there.
     * Packets are received by the manager and demultiplexed to the connection matching the sender, hence the need for a hand off.
     *
     * @param received  the received packets that will be placed in queue
     */
    void handOff(Packet.UDP received) {
        this.received.add(received);
    }
}
