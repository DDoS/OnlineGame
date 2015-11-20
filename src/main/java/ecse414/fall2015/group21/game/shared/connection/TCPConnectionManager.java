package ecse414.fall2015.group21.game.shared.connection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 *
 */
public class TCPConnectionManager implements ConnectionManager<TCPConnection> {
    // Maps player number to connection
    private final Map<Integer, TCPConnection> openConnections = new HashMap<>();
    private final Queue<Message> unconnectedMessages = new LinkedList<>();

    @Override
    public void init(Address receiveAddress) {
        // Open port for connections
    }

    @Override
    public void update() {
        // Accept new connections as pending, add request messages to unconnected messages for approval
    }

    @Override
    public TCPConnection openConnection(Address sendAddress, int playerNumber) {
        // Accept pending connection
        return null;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Refuse pending connection
    }

    @Override
    public void closeAll() {

    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
