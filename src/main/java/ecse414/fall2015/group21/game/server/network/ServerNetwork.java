package ecse414.fall2015.group21.game.server.network;

import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.connection.ConnectionManager;
import ecse414.fall2015.group21.game.shared.connection.UDPConnectionManager;
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
        connections.update();
        final Queue<Message> messages = connections.getUnconnectedMessages();
        while (!messages.isEmpty()) {
            System.out.println(messages.poll().getType());
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
