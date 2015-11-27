package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 */
public class TCPConnectionManager implements ConnectionManager  {
    // Maps player number to connection
    private final Map<Integer, TCPConnection> openConnections = new HashMap<>();
    private final Set<Address> connected = new HashSet<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ServerBootstrap b = new ServerBootstrap();
    private final TCPConnectionInitializer initializer = new TCPConnectionInitializer();

    private class TCPConnectionInitializer extends ChannelInitializer<SocketChannel> {
        private final Queue<Pending> pendingConnections = new ConcurrentLinkedQueue<>();

        protected class Pending {
            private final Address address;
            private final SocketChannel channel;
            private final TCPConnectionHandler handler;
            protected Pending(Address address, SocketChannel channel, TCPConnectionHandler handler) {
                this.address = address;
                this.channel = channel;
                this.handler = handler;
            }
            protected Address getAddress() {
                return this.address;
            }
            protected SocketChannel getChannel() {
                return this.channel;
            }
            protected TCPConnectionHandler getHandler() {
                return this.handler;
            }
        }

        protected void getPendingMessages(Queue<Message> queue) {
            for(Pending client : pendingConnections) {
                // Get All messages from the handler queues
                Queue<Packet.TCP> received = new LinkedList<>();
                client.handler.readPackets(received);
                // If we have connection request packets, add these to the connection message queue
                for(Packet.TCP packet : received) {
                    if(packet instanceof ConnectRequestPacket) {
                        TCPDecoder.INSTANCE.decode(packet, client.getAddress(), queue);
                    }
                }
            }
        }


        protected Pending getPending(Address address) {
            for(Pending list : pendingConnections) {
                if(list.getAddress().equals(address)) {
                    pendingConnections.remove(list);
                    return list;
                }
            }
            return null;
        }

        protected void refuseAllPending() {
            pendingConnections.forEach(client -> client.getChannel().close());
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            // Create a new handler
            TCPConnectionHandler handler = new TCPConnectionHandler();
            // Setup the pipeline for the new socket
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(handler);

            // Add the new connection to the pendingConnections map
            int ip = Address.ipAddressFromBytes(ch.remoteAddress().getAddress().getAddress());
            Address sender = Address.forUnconnectedRemoteClient(ip, (short) ch.remoteAddress().getPort());
            Pending newConnection = new Pending(sender, ch, handler);
            pendingConnections.add(newConnection);
        }
    }

    @Override
    public void init(Address receiveAddress) {
        this.receiveAddress = receiveAddress;
        try{
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(initializer);
            b.bind(receiveAddress.getPort()).sync().channel();
        } catch(InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            throw new RuntimeException(e);
        }
        System.out.println("Opened a new receiver on: " + receiveAddress.getPort());
    }

    @Override
    public void update() {
        // TODO: accept new connections as pending, add request messages to unconnected for approval
        // Get all the packets from the initializer
        initializer.getPendingMessages(unconnectedMessages);

    }

    @Override
    public TCPConnection openConnection(Address sendAddress, int playerNumber) {
        // Accept pending connection
        if (openConnections.containsKey(playerNumber) || connected.contains(sendAddress)) {
            throw new IllegalStateException("Connection for player " + playerNumber + " is already open");
        }
        TCPConnectionInitializer.Pending pendingConnection = initializer.getPending(sendAddress);
        if(pendingConnection == null) {
            throw new IllegalStateException("No pending connection for " + sendAddress);
        }
        final TCPConnection connection = new TCPConnection(receiveAddress, sendAddress,
                pendingConnection.getChannel(), pendingConnection.getHandler());
        openConnections.put(playerNumber, connection);
        connected.add(sendAddress);
        return connection;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Refuse pending connection
        // TODO: close temp connection if still open
        TCPConnectionInitializer.Pending pendingConnection = initializer.getPending(sourceAddress);
        if(pendingConnection != null) {
            pendingConnection.getChannel().close();
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
        initializer.refuseAllPending();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
