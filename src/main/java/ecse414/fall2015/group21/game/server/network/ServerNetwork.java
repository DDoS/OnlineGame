package ecse414.fall2015.group21.game.server.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.client.network.ClientNetwork;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.ConnectionManager;
import ecse414.fall2015.group21.game.shared.connection.UDPConnectionManager;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The server side of the networking layer
 */
public class ServerNetwork extends TickingElement {
    private static final int PLAYER_LIMIT = 15;
    private final Universe universe;
    private ConnectionManager connections;
    private final Map<Integer, Long> lastEventTimes = new HashMap<>();
    private int playerCount = 0;
    private int playerIndex = 0;

    public ServerNetwork(Universe universe) {
        super("ServerNetwork", 20);
        this.universe = universe;
    }

    @Override
    public void onStart() {
        connections = new UDPConnectionManager();
        connections.init(Main.ARGUMENTS.address());
    }

    @Override
    public void onTick(long dt) {
        final Queue<Message> reusedQueue = new LinkedList<>();
        // Perform some internal update of connections
        connections.update();
        // Process unconnected messages
        final Queue<Message> unconnectedMessages = connections.getUnconnectedMessages();
        while (!unconnectedMessages.isEmpty()) {
            final Message message = unconnectedMessages.poll();
            // Process connection requests only
            if (message.getType() == Message.Type.CONNECT_REQUEST) {
                reusedQueue.clear();
                processConnectRequest((ConnectRequestMessage) message, reusedQueue);
            }
        }
        // Process messages from each connection
        final Queue<Integer> timeouts = new LinkedList<>();
        for (Map.Entry<Integer, ? extends Connection> entry : connections.getConnections().entrySet()) {
            final Integer playerNumber = entry.getKey();
            // First check that the connection is still alive
            if (timedOut(playerNumber)) {
                timeouts.add(playerNumber);
                break;
            }
            // Get the connection, read the messages
            final Connection connection = entry.getValue();
            reusedQueue.clear();
            connection.receive(reusedQueue);
            // Process them
            processPlayerMessages(playerNumber, connection, reusedQueue);
        }
        // Disconnect timeouts
        timeouts.forEach((playerNumber) -> {
            connections.closeConnection(playerNumber);
            lastEventTimes.remove(playerNumber);
            playerCount--;
            System.out.println("Connection timed out for player " + playerNumber);
        });
    }

    private void processConnectRequest(ConnectRequestMessage message, Queue<Message> reusedQueue) {
        // Connect is not already connected and we have room
        if (playerCount < PLAYER_LIMIT && !connections.isConnected(message.address)) {
            // Open the connection, reply with a fulfill message
            final int playerNumber = playerIndex++;
            final Connection connection = connections.openConnection(message.address, playerNumber);
            reusedQueue.add(new ConnectFulfillMessage((short) playerNumber, universe.getSeed()));
            connection.send(reusedQueue);
            lastEventTimes.put(playerNumber, System.nanoTime());
            playerCount++;
            System.out.println("Connected " + message.address + " as player " + playerNumber);
        } else {
            // Refuse connection, allow TCP to close it
            connections.refuseConnection(message.address);
            System.out.println("Refused " + message.address);
        }
    }

    private boolean timedOut(int playerNumber) {
        final long currentEventTime = System.nanoTime();
        final long lastEventTime = lastEventTimes.get(playerNumber);
        // Check time elapsed since last connect/time request
        return currentEventTime - lastEventTime > ClientNetwork.TIME_REQUEST_PERIOD * 2;
    }

    private void processPlayerMessages(int playerNumber, Connection connection, Queue<Message> messages) {
        final Queue<Message> toSend = new LinkedList<>();
        // Create a list of replies for the received messages
        while (!messages.isEmpty()) {
            final Message message = messages.poll();
            switch (message.getType()) {
                case TIME_REQUEST:
                    // Send a fulfill with the time as the reply
                    final TimeRequestMessage timeRequest = (TimeRequestMessage) message;
                    toSend.add(new TimeFulfillMessage(timeRequest.requestNumber, universe.getTime()));
                    lastEventTimes.put(playerNumber, System.nanoTime());
                    break;
                case PLAYER_STATE:
                case PLAYER_SHOOT:
                case PLAYER_HEALTH:
                    // Move to universe
                    universe.handOff(message);
                    break;
                default:
                    // Not a message the server should care about
                    break;
            }
        }
        // Send all out replies
        connection.send(toSend);
    }

    @Override
    public void onStop() {
        connections.closeAll();
        connections = null;
    }

    public Universe getUniverse() {
        return this.universe;
    }
}
