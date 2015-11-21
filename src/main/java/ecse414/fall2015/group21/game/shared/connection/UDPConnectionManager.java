package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 *
 */
public class UDPConnectionManager implements ConnectionManager<UDPConnection> {
    private final Map<Integer, UDPConnection> openConnections = new HashMap<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;

    @Override
    public void init(Address receiveAddress) {
        this.receiveAddress = receiveAddress;
        // TODO: open port for connections
    }

    @Override
    public void update() {
        // TODO: de-multiplex player packets, handle connection requests
        // openConnection.buffer(packet)
    }

    @Override
    public UDPConnection openConnection(Address sendAddress, int playerNumber) {
        // Create new connection information
        final UDPConnection connection = new UDPConnection(sendAddress, receiveAddress);
        openConnections.put(playerNumber, connection);
        return connection;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Do nothing
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(UDPConnection::close);
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
