package ecse414.fall2015.group21.game.shared.connection;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 * A full duplex connection between a client and server. Not bound to a particular side.
 */
public interface Connection {
    /**
     * Sends to the opposite entity.
     *
     * @param queue The messages to send
     */
    void send(Queue<? extends Message> queue);

    /**
     * Read what was received from the opposite entity.
     *
     * @param queue The messages that were received
     */
    void receive(Queue<? super Message> queue);

    /**
     * Closes the connection.
     */
    void close();
}
