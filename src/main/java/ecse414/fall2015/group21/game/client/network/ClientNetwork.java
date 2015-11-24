package ecse414.fall2015.group21.game.client.network;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.UDPConnection;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The client side of the networking layer
 */
public class ClientNetwork extends TickingElement {
    private final RemoteUniverse universe;
    private Connection connection;
    private boolean connected = false;

    public ClientNetwork(RemoteUniverse universe) {
        super("ClientNetwork", 20);
        this.universe = universe;
    }

    @Override
    public void onStart() {
        connection = new UDPConnection(Address.defaultUnconnectedLocalClient(), Main.ARGUMENTS.address());
    }

    @Override
    public void onTick(long dt) {
        final Queue<Message> messages = new LinkedList<>();
        if (!connected) {
            // Start by checking for a connection fulfill
            connection.receive(messages);
            for (Message message : messages) {
                if (message.getType() == Message.Type.CONNECT_FULFILL) {
                    final ConnectFulfillMessage fulfill = (ConnectFulfillMessage) message;
                    // TODO: pass info to universe
                    System.out.println("Connected to " + connection.getRemote() + " as player " + fulfill.playerNumber);
                    connected = true;
                    break;
                }
            }
            // No connection fulfill, try again
            if (!connected) {
                // TODO: add timer before retry
                // Try to connect
                messages.clear();
                messages.add(new ConnectRequestMessage(connection.getLocal()));
                connection.send(messages);
            }
        }
    }

    @Override
    public void onStop() {
        connected = false;
        connection.close();
        connection = null;
    }
}
