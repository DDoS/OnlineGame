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

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.codec.TCPEncoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Represents a TCP connection. It holds the addresses (IP and port) of both sides of the connection.
 * Responsible for reading and sending TCP packets.
 */
public class TCPConnection implements Connection {
    private Address local;
    private final Address remote;
    private final Channel channel;
    private final TCPConnectionHandler handler;
    private EventLoopGroup group = null;

    /**
     * Creates a new TCP connection.
     *
     * @param local The local address of the connection (port)
     * @param remote The remote address to send to (ip and port)
     */
    public TCPConnection(Address local, Address remote) {
        this.local = local;
        this.remote = remote;
        group = new NioEventLoopGroup();
        handler = new TCPConnectionHandler();
        try {
            // Bootstrap documentation says to use connect() methods instead of bind() for TCP
            channel = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(handler)
                    .localAddress(local.getPort())
                    .connect(remote.asInetSocketAddress())
                    .channel();
            channel.pipeline().addFirst(new TCPConnectionDecoder());
        } catch (Exception exception) {
            group.shutdownGracefully();
            throw new RuntimeException("Failed to create channel at " + local, exception);
        }
        System.out.println("Connected " + local + " to " + remote);
    }

    /**
     * Creates a TCP connection from an existing channel.
     *
     * @param local The local address of the connection (port)
     * @param remote The remove address to be sent to (ip and port)
     * @param channel The existing Channel used for sending
     * @param handler The existing handler for this channel
     */
    public TCPConnection(Address local, Address remote, Channel channel, TCPConnectionHandler handler) {
        this.local = local;
        this.remote = remote;
        this.channel = channel;
        this.handler = handler;
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
        // Encode messages to packets
        queue.forEach(message -> TCPEncoder.INSTANCE.encode(message, local, remote, encoded));
        // Write packets to channel as byte buffers
        encoded.forEach(packet -> channel.write(packet.asRaw()));
        // Flush the channel to ensure all bytes are written out
        channel.flush();
    }

    @Override
    public void receive(Queue<? super Message> queue) {
        final Queue<Packet.TCP> received = new LinkedList<>();
        handler.readPackets(received);
        // Decode packets using factory and add them to the queue
        for (Packet.TCP packet : received) {
            TCPDecoder.INSTANCE.decode(packet, remote, queue);
        }
    }

    @Override
    public void close() {
        // Attempting to close connection...
        if (channel.isActive()) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}
