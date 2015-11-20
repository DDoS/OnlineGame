package ecse414.fall2015.group21.game.shared.codec;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 * Decodes packets to messages and places them in the queue.
 */
public interface Decoder<T extends Packet> {
    /**
     * Decodes a packet and places it in the queue.
     *
     * @param packet The packet to decode
     * @param source The source address of the packet, or null if unknown (example: UDP)
     * @param queue The queue in which to places the decoded packets as messages
     */
    void decode(T packet, Address source, Queue<? super Message> queue);
}
