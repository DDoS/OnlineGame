package ecse414.fall2015.group21.game.shared.codec;

import java.util.Collection;

import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;

/**
 *
 */
public interface Decoder<T extends Packet> {
    void decode(T packet, Collection<? extends Message> queue);
}
