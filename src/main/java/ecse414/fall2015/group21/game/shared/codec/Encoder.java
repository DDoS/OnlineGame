package ecse414.fall2015.group21.game.shared.codec;

import java.util.Collection;

import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public interface Encoder<T extends Packet> {
    void encode(Message message, Collection<? extends T> queue);
}
