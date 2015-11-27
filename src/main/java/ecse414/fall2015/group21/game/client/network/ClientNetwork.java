package ecse414.fall2015.group21.game.client.network;

import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.UDPConnection;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.util.TickingElement;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * The client side of the networking layer
 */
public class ClientNetwork extends TickingElement {
    private static final long CONNECT_REQUEST_TIMEOUT = 1_000_000_000L; // ns
    public static final long TIME_REQUEST_PERIOD = 1_500_000_000L; // ns
    private static final int MISSED_TIME_REQUEST_THRESHOLD = 2;
    private final RemoteUniverse universe;
    private Connection connection;
    private boolean connected = false;
    private long lastEventTime = 0;
    private int timeRequestNumber = -1;
    private int timeFulfillNumber = -1;

    public ClientNetwork(RemoteUniverse universe) {
        super("ClientNetwork", 60);
        this.universe = universe;
    }

    @Override
    public void onStart() {
        connection = new UDPConnection(Address.defaultUnconnectedLocalClient(), Main.ARGUMENTS.address());
    }

    @Override
    public void onTick(long dt) {
        // Read received messages
        final Queue<Message> messages = new LinkedList<>();
        connection.receive(messages);
        // Try to connect is not done yet and do nothing else
        if (!connected) {
            attemptConnection(messages);
            return;
        }
        // Process received messages
        for (Message message : messages) {
            switch (message.getType()) {
                case TIME_FULFILL:
                    processTimeFulfill((TimeFulfillMessage) message);
                    break;
                case PLAYER_STATE:
                case PLAYER_SHOOT:
                case PLAYER_HEALTH:
                    // Move to universe
                    universe.handOff(message);
                    break;
                default:
                    // Not a message we care about
                    break;
            }
        }
        // Check for a connection a timeout
        if (timeRequestNumber - timeFulfillNumber >= MISSED_TIME_REQUEST_THRESHOLD) {
            // Disconnect and kill player locally
            connected = false;
            universe.handOff(new PlayerMessage(Message.Type.PLAYER_HEALTH, 0, Vector2f.ZERO, Complexf.IDENTITY, (short) 0, universe.getUserPlayerNumber()));
            System.out.println("Connection timed out, missed " + MISSED_TIME_REQUEST_THRESHOLD + " stay-alive packets");
            return;
        }
        messages.clear();
        // Send new messages as needed
        final long currentEventTime = System.nanoTime();
        // Check if it's time for a new time request
        if (currentEventTime - lastEventTime > TIME_REQUEST_PERIOD) {
            messages.add(new TimeRequestMessage(++timeRequestNumber));
            lastEventTime = currentEventTime;
        }
        // Send a new player state
        final Player player = universe.getUserPlayer();
        if (player != null) {
            messages.add(new PlayerMessage(Message.Type.PLAYER_STATE, player, false));
        }
        // Add events to send
        final Queue<Message> events = universe.getEvents();
        while (!events.isEmpty()) {
            messages.add(events.poll());
        }
        // Send the messages as a bunch
        connection.send(messages);
    }

    private void attemptConnection(Queue<Message> messages) {
        // Start by checking for a connection fulfill
        for (Message message : messages) {
            if (message.getType() == Message.Type.CONNECT_FULFILL) {
                final ConnectFulfillMessage fulfill = (ConnectFulfillMessage) message;
                universe.handOff(fulfill);
                connected = true;
                System.out.println("Connected to " + connection.getRemote() + " as player " + fulfill.playerNumber);
                break;
            }
        }
        // No connection fulfill, try again
        if (!connected) {
            final long currentEventTime = System.nanoTime();
            if (currentEventTime - lastEventTime > CONNECT_REQUEST_TIMEOUT) {
                // Try to connect
                messages.clear();
                messages.add(new ConnectRequestMessage(connection.getLocal()));
                connection.send(messages);
                lastEventTime = currentEventTime;
                System.out.println("Attempting to connect to " + connection.getRemote());
            }
        }
    }

    private void processTimeFulfill(TimeFulfillMessage message) {
        // Make sure the message isn't late, should be the latest request
        if (message.requestNumber == timeRequestNumber) {
            // Adjust time for RTT and move to universe
            universe.handOff(new TimeFulfillMessage(timeRequestNumber, message.time + (System.nanoTime() - lastEventTime) / 2));
            timeFulfillNumber = timeRequestNumber;
        }
    }

    @Override
    public void onStop() {
        connected = false;
        connection.close();
        connection = null;
    }
}
