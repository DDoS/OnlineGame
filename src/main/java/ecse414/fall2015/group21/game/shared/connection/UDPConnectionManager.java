package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import ecse414.fall2015.group21.game.shared.codec.UDPDecoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;
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
public class UDPConnectionManager implements ConnectionManager {
    private final Map<Integer, UDPConnection> openConnections = new HashMap<>();
    private final Set<Address> connected = new HashSet<>();
    private final Map<Integer, Integer> secretToNumber = new HashMap<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;
    private Random secretGenerator;
    private Channel channel;
    private EventLoopGroup group;
    private UDPConnectionHandler handler;

    @Override
    public void init(Address receiveAddress) {
        // Secret generator is unique for each manager lifetime
        secretGenerator = new Random();
        this.receiveAddress = receiveAddress;
        // Create thread group and handler
        group = new NioEventLoopGroup();
        handler = new UDPConnectionHandler();
        // Initialize listening channel
        try {
            channel = new Bootstrap()
                    .group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(handler)
                    .bind(receiveAddress.getPort()).syncUninterruptibly()
                    .channel();
        } catch (Exception exception) {
            group.shutdownGracefully();
            throw new RuntimeException("Failed to create channel at " + receiveAddress, exception);
        }
        System.out.println("Listening at " + receiveAddress.toString());
    }

    @Override
    public void update() {
        final Queue<DatagramPacket> connectedPackets = new LinkedList<>();
        handler.readPackets(connectedPackets);
        // De-multiplex packets
        for (DatagramPacket rawPacket : connectedPackets) {
            final InetSocketAddress sender = rawPacket.sender();
            final Packet.UDP packet = Packet.Type.UDP_FACTORY.newInstance(rawPacket.content());
            // Release the raw packet, don't need it anymore
            rawPacket.release();
            // Look for a connected packet
            final int sharedSecret;
            if (packet instanceof TimeRequestPacket.UDP) {
                sharedSecret = ((TimeRequestPacket.UDP) packet).sharedSecret;
            } else if (packet instanceof PlayerPacket.UDP) {
                sharedSecret = ((PlayerPacket.UDP) packet).sharedSecret;
            } else {
                // Not a connected packet, decode and place in unconnected messages
                UDPDecoder.INSTANCE.decode(packet, Address.forUnconnectedRemoteClient(Address.ipAddressFromBytes(sender.getAddress().getAddress()), sender.getPort()), unconnectedMessages);
                continue;
            }
            // Get player number from secret, use it to get the connection
            final int playerNumber = secretToNumber.get(sharedSecret);
            final UDPConnection connection = openConnections.get(playerNumber);
            if (connection == null) {
                throw new IllegalStateException("Expected an open connection for player: " + playerNumber);
            }
            // Hand over packet to connection
            connection.handOff(packet);
        }
    }

    @Override
    public UDPConnection openConnection(Address sendAddress, int playerNumber) {
        if (openConnections.containsKey(playerNumber) || connected.contains(sendAddress)) {
            throw new IllegalStateException("Connection for player " + playerNumber + " is already open");
        }
        // Generate shared secret
        final int secret = generateSecret();
        secretToNumber.put(secret, playerNumber);
        sendAddress = sendAddress.connectClient(secret);
        // Create new connection
        final UDPConnection connection = new UDPConnection(receiveAddress, sendAddress, channel);
        // Store it and return it
        openConnections.put(playerNumber, connection);
        connected.add(sendAddress);
        return connection;
    }

    private int generateSecret() {
        int secret = secretGenerator.nextInt();
        while (secretToNumber.containsKey(secret)) {
            secret = secretGenerator.nextInt();
        }
        return secret;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Nothing to do
    }

    @Override
    public Connection getConnection(int playerNumber) {
        final UDPConnection connection = openConnections.get(playerNumber);
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
        final UDPConnection connection = openConnections.remove(playerNumber);
        if (connection != null) {
            connection.close();
            final Address remote = connection.getRemote();
            connected.remove(remote);
            secretToNumber.remove(remote.getSharedSecret());
        }
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(UDPConnection::close);
        group.shutdownGracefully();
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
