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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Manager for TCP connection. Responsible for keeping track of connections, keeps a map linking Player number and connections.
 */
public class TCPConnectionManager extends ChannelInitializer<SocketChannel> implements ConnectionManager {
    private final Map<Integer, TCPConnection> openConnections = new HashMap<>();
    private final Set<Address> connected = new HashSet<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final Map<Address, TCPConnection> pendingConnections = new ConcurrentHashMap<>();
    private Address receiveAddress;

    @Override
    public void init(Address receiveAddress) {
        this.receiveAddress = receiveAddress;
        try {
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(this)
                    .bind(receiveAddress.getPort()).sync()
                    .channel();
        } catch (Exception exception) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw new RuntimeException("Failed to create channel at " + receiveAddress, exception);
        }
        System.out.println("Listening at " + receiveAddress);
    }

    @Override
    public void update() {
        final Queue<Packet.TCP> received = new LinkedList<>();
        for (TCPConnection client : pendingConnections.values()) {
            // Get all messages from the pending connections
            received.clear();
            client.receive(unconnectedMessages);
            received.forEach(packet -> TCPDecoder.INSTANCE.decode(packet, client.getRemote(), unconnectedMessages));
        }
    }

    @Override
    public TCPConnection openConnection(Address sendAddress, int playerNumber) {
        System.out.println("Opening a connection for player " + playerNumber + " on Port " + sendAddress.getPort());
        // Accept pending connection
        if (openConnections.containsKey(playerNumber) || connected.contains(sendAddress)) {
            throw new IllegalStateException("Connection for player " + playerNumber + " is already open");
        }
        final TCPConnection connection = pendingConnections.remove(sendAddress);
        if (connection == null) {
            throw new IllegalStateException("No pending connection for " + sendAddress);
        }
        openConnections.put(playerNumber, connection);
        connected.add(sendAddress);
        return connection;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        final TCPConnection connection = pendingConnections.remove(sourceAddress);
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public Connection getConnection(int playerNumber) {
        final TCPConnection connection = openConnections.get(playerNumber);
        if (connection == null) {
            throw new IllegalArgumentException("No connection open for " + playerNumber);
        }
        return connection;
    }

    @Override
    public Map<Integer, ? extends Connection> getConnections() {
        return openConnections;
    }

    @Override
    public boolean isConnected(Address remote) {
        return connected.contains(remote);
    }

    @Override
    public void closeConnection(int playerNumber) {
        final TCPConnection connection = openConnections.remove(playerNumber);
        if (connection != null) {
            connection.close();
            connected.remove(connection.getRemote());
        }
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(TCPConnection::close);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        pendingConnections.forEach((k, v) -> v.close());
        pendingConnections.clear();
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        // Create a new handler
        final TCPConnectionHandler handler = new TCPConnectionHandler();
        // Setup the pipeline for the new socket
        channel.pipeline().addLast(new TCPConnectionDecoder(), handler);
        // Add the new connection to the pendingConnections map
        final InetSocketAddress remoteAddress = channel.remoteAddress();
        final int ip = Address.ipAddressFromBytes(remoteAddress.getAddress().getAddress());
        final int port = remoteAddress.getPort();
        final Address remote = Address.forRemoteClient(ip, port, 0);
        pendingConnections.put(remote, new TCPConnection(receiveAddress, remote, channel, handler));
    }
}
