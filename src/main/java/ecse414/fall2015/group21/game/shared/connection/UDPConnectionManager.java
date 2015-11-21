package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import ecse414.fall2015.group21.game.shared.codec.UDPDecoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;

/**
 *
 */
public class UDPConnectionManager implements ConnectionManager<UDPConnection> {
    private final Map<Integer, UDPConnection> openConnections = new HashMap<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;
    private int xorMangler;

    @Override
    public void init(Address receiveAddress) {
        // Secret generator unique for each manager lifetime
        xorMangler = new Random().nextInt();
        this.receiveAddress = receiveAddress;
        // TODO: open port for connections
    }

    @Override
    public void update() {
        // TODO: read channel, create packets, and store in this queue
        final Queue<Packet.UDP> connectedPackets = new LinkedList<>();
        // De-multiplex packets
        for (Packet.UDP packet : connectedPackets) {
            // Look for connected packet
            final int sharedSecret;
            if (packet instanceof TimeRequestPacket.UDP) {
                sharedSecret = ((TimeRequestPacket.UDP) packet).sharedSecret;
            } else if (packet instanceof PlayerPacket.UDP) {
                sharedSecret = ((PlayerPacket.UDP) packet).sharedSecret;
            } else {
                // Not a connected packet, decode and place in unconnected messages
                UDPDecoder.INSTANCE.decode(packet, null, unconnectedMessages);
                continue;
            }
            // Get player number from secret, use it to get the connection
            final int playerNumber = secretToNumber(sharedSecret);
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
        if (openConnections.containsKey(playerNumber)) {
            throw new IllegalStateException("Connection for player " + playerNumber + " is already open");
        }
        // Generate shared secret
        sendAddress = sendAddress.connectClient(numberToSecret(playerNumber));
        // Create new connection
        final UDPConnection connection = new UDPConnection(sendAddress, receiveAddress);
        // Store it and return it
        openConnections.put(playerNumber, connection);
        return connection;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Nothing to do
    }

    @Override
    public Optional<UDPConnection> getConnection(int playerNumber) {
        return Optional.ofNullable(openConnections.get(playerNumber));
    }

    @Override
    public void closeConnection(int playerNumber) {
        final UDPConnection connection = openConnections.remove(playerNumber);
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(UDPConnection::close);
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }

    private int numberToSecret(int number) {
        // Xor is one-to-one so all numbers will be unique
        return number ^ xorMangler;
    }

    private int secretToNumber(int secret) {
        // Xor is its own reciprocal!
        return numberToSecret(secret);
    }
}
