package ecse414.fall2015.group21.game.shared.codec;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 * Encodes messages to packets and places them in the queue.
 */
public interface Encoder<T extends Packet> {
    /**
     * Encodes a message and places it in the queue.
     *  @param message The message to decode
     * @param source The source address of the message
     * @param destination The destination address of the message
     * @param queue The queue in which to places the encoded message as packets
     */
    void encode(Message message, Address source, Address destination, Queue<? super T> queue);
}
