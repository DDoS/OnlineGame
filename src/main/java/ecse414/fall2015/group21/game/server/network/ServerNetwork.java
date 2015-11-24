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
    private final Universe universe;
    private ConnectionManager connections;

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
        connections.update();
        final Queue<Message> messages = connections.getUnconnectedMessages();
        while (!messages.isEmpty()) {
            reusedQueue.clear();
            final Message message = messages.poll();
            switch (message.getType()) {
                case CONNECT_REQUEST:
                    processConnectRequest((ConnectRequestMessage) message, reusedQueue);
                    break;
                case TIME_REQUEST:
                    break;
                case PLAYER_STATE:
                    break;
                case PLAYER_SHOOT:
                    break;
                case PLAYER_HEALTH:
                    break;
                default:
                    // Not a message the server should care about
                    break;
            }
        }
        for (Player player : universe.getPlayers()) {
            reusedQueue.clear();
            final Connection connection = connections.getConnection(player.getNumber());
            connection.receive(reusedQueue);
            System.out.println("player " + player.getNumber() + ": " + reusedQueue);
        }
    }

    private void processConnectRequest(ConnectRequestMessage message, Queue<Message> reusedQueue) {
        if (!universe.isFull() && !connections.isConnected(message.address)) {
            final int playerNumber = universe.getPlayers().size();
            final Connection connection = connections.openConnection(message.address, playerNumber);
            reusedQueue.add(new ConnectFulfillMessage((short) playerNumber, universe.getSeed()));
            connection.send(reusedQueue);
            System.out.println("Connected " + message.address + " as player " + playerNumber);
        } else {
            connections.refuseConnection(message.address);
            System.out.println("Refused " + message.address);
        }
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
