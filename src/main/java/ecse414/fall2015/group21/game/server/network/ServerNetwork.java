package ecse414.fall2015.group21.game.server.network;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.ConnectionManager;
import ecse414.fall2015.group21.game.shared.connection.UDPConnectionManager;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The server side of the networking layer
 */
public class ServerNetwork extends TickingElement {
    private static final int PLAYER_LIMIT = 15;
    private final Universe universe;
    private ConnectionManager connections;
    private int playerCount = 0;

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
        // Process messages from each player
        for (Player player : universe.getPlayers()) {
            // Get the connection, read the messages
            final Connection connection = connections.getConnection(player.getNumber());
            reusedQueue.clear();
            connection.receive(reusedQueue);
            // Process them
            processPlayerMessages(reusedQueue);
            System.out.println("player " + player.getNumber() + ": " + reusedQueue);
        }
    }

    private void processConnectRequest(ConnectRequestMessage message, Queue<Message> reusedQueue) {
        // Connect is not already connected and we have room
        if (playerCount < PLAYER_LIMIT && !connections.isConnected(message.address)) {
            // Open the connection, reply with a fulfill message
            final Connection connection = connections.openConnection(message.address, playerCount);
            reusedQueue.add(new ConnectFulfillMessage((short) playerCount, universe.getSeed()));
            connection.send(reusedQueue);
            System.out.println("Connected " + message.address + " as player " + playerCount);
            playerCount++;
        } else {
            // Refuse connection, allow TCP to close it
            connections.refuseConnection(message.address);
            System.out.println("Refused " + message.address);
        }
    }

    private void processPlayerMessages(Queue<Message> messages) {
        while (!messages.isEmpty()) {
            final Message message = messages.poll();
            switch (message.getType()) {
                case TIME_REQUEST:
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
    }

    @Override
    public void onStop() {
        connections.closeAll();
        connections = null;
        playerCount = 0;
    }

    public Universe getUniverse() {
        return this.universe;
    }
}
