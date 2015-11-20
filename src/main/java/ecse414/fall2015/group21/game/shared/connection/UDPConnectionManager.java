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

    @Override
    public void init(Address receiveAddress) {
        // Open port for connections
    }

    @Override
    public void update() {
        // De-multiplex player packets, handle connection requests
    }

    @Override
    public UDPConnection openConnection(Address sendAddress, int playerNumber) {
        // Create new connection information
        return null;
    }

    @Override
    public void refuseConnection(Address sourceAddress) {
        // Do nothing
    }

    @Override
    public void closeAll() {

    }

    @Override
    public Queue<Message> getUnconnectedMessages() {
        return unconnectedMessages;
    }
}
