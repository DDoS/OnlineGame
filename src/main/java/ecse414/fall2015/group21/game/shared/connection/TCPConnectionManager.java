package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 *
 */
public class TCPConnectionManager implements ConnectionManager<TCPConnection> {
    // Maps player number to connection
    private final Map<Integer, TCPConnection> openConnections = new HashMap<>();
    private final Set<Address> pendingConnections = new HashSet<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ServerBootstrap b = new ServerBootstrap();
    private final TCPConnectionInitializer initializer = new TCPConnectionInitializer();

    @Override
    public void init(Address receiveAddress) {
        this.receiveAddress = receiveAddress;
        try{
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(initializer);
            b.bind(receiveAddress.getPort()).sync().channel().closeFuture().sync();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update() {
        // TODO: accept new connections as pending, add request messages to unconnected for approval
        // Get all the packets from the initialiser
        initializer.getPendingMessages(unconnectedMessages);

    }

    @Override
    public TCPConnection openConnection(Address sendAddress, int playerNumber) {
        // Accept pending connection
        if (openConnections.containsKey(playerNumber)) {
            throw new IllegalStateException("Connection for player " + playerNumber + " is already open");
        }
        if (!pendingConnections.remove(sendAddress)) {
            throw new IllegalStateException("No pending connection for " + sendAddress);
        }
        // TODO: also pass netty connection objects to constructor?
        final TCPConnection connection = new TCPConnection(receiveAddress, sendAddress);
        openConnections.put(playerNumber, connection);
        return connection;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Refuse pending connection
        pendingConnections.remove(sourceAddress);
        // TODO: close temp connection if still open
    }

    @Override
    public Optional<TCPConnection> getConnection(int playerNumber) {
        return Optional.ofNullable(openConnections.get(playerNumber));
    }

    @Override
    public void closeConnection(int playerNumber) {
        final TCPConnection connection = openConnections.remove(playerNumber);
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(TCPConnection::close);
        pendingConnections.forEach(this::refuseConnection);
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
