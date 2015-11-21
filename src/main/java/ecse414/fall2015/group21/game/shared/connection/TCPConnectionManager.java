package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 *
 */
public class TCPConnectionManager implements ConnectionManager<TCPConnection> {
    // Maps player number to connection
    private final Map<Integer, TCPConnection> openConnections = new HashMap<>();
    private final Set<Address> pendingConnections = new HashSet<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();
    private Address receiveAddress;

    @Override
    public void init(Address receiveAddress) {
        this.receiveAddress = receiveAddress;
        // TODO: open port for connections
    }

    @Override
    public void update() {
        // TODO: accept new connections as pending, add request messages to unconnected for approval
        // pendingConnections.add(sourceAddress)
    }

    @Override
    public TCPConnection openConnection(Address sendAddress, int playerNumber) {
        // Accept pending connection
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
        // TODO: close temp connection
    }

    @Override
    public void closeAll() {
        openConnections.values().forEach(TCPConnection::close);
        pendingConnections.forEach(this::refuseConnection);
    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
