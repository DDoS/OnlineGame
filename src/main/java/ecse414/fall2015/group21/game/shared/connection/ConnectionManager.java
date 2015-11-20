package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetSocketAddress;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 * Manages connections from server to client.
 */
public interface ConnectionManager<T extends Connection> {
    /**
     * Initiates the connection manager with a received address for incoming connections.
     *
     * @param receiveAddress The address clients should be connecting to
     */
    void init(InetSocketAddress receiveAddress);

    /**
     * Called periodically to give the manager a chance to read the channel.
     */
    void update();

    /**
     * Opens a connection to a client, given its address and additional optional arguments. Throws an error on failure. Returns the connection if it already exists.
     *
     * @param sendAddress The address of the client
     * @param optionalInfo Additional info, such as the player number and shared secret
     * @return The opened connection on success
     */
    T openConnection(InetSocketAddress sendAddress, Object... optionalInfo);

    /**
     * Refuses a connection, this is the opposite of {@link #openConnection(InetSocketAddress, Object...)}. Not necessary for UDP, but needed for TCP. Does nothing if the address never requested a
     * connection.
     *
     * @param sourceAddress The address that sought connection to the server but was refused
     */
    void refuseConnection(InetSocketAddress sourceAddress);

    /**
     * Closes all existing connections.
     */
    void closeAll();

    /**
     * Returns messages received that aren't part of a connection. These may be connection requests.
     *
     * @return Messages that aren't part of any connection
     */
    Queue<Message> getUnconnectedMessages();
}
