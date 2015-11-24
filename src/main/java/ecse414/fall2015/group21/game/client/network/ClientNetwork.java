package ecse414.fall2015.group21.game.client.network;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.UDPConnection;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The client side of the networking layer
 */
public class ClientNetwork extends TickingElement {
    private final RemoteUniverse universe;
    private Connection connection;

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
        messages.add(new ConnectRequestMessage(Address.defaultUnconnectedLocalClient()));
        connection.send(messages);
        messages.clear();
        connection.receive(messages);
        messages.forEach(System.out::println);
    }

    @Override
    public void onStop() {
        connection.close();
        connection = null;
    }
}
