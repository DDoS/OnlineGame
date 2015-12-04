package ecse414.fall2015.group21.game.shared.codec;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Decodes TCP packets into messages and places them in a queue.
 */
public final class TCPDecoder implements Decoder<Packet.TCP> {
    public static final TCPDecoder INSTANCE = new TCPDecoder();

    private TCPDecoder() {
    }

    @Override
    public void decode(Packet.TCP packet, Address source, Queue<? super Message> queue) {
        switch (packet.getType()) {
            case CONNECT_REQUEST:
                queue.add(new ConnectRequestMessage(source));
                break;
            case CONNECT_FULFILL:
                final ConnectFulfillPacket.TCP connectFulfillPacket = (ConnectFulfillPacket.TCP) packet;
                queue.add(new ConnectFulfillMessage(connectFulfillPacket.playerNumber, connectFulfillPacket.seed, connectFulfillPacket.time));
                break;
            case TIME_REQUEST:
                final TimeRequestPacket.TCP timeRequestPacket = (TimeRequestPacket.TCP) packet;
                queue.add(new TimeRequestMessage(timeRequestPacket.requestNumber));
                break;
            case TIME_FULFILL:
                final TimeFulfillPacket.TCP timeFulfillPacket = (TimeFulfillPacket.TCP) packet;
                queue.add(new TimeFulfillMessage(timeFulfillPacket.requestNumber, timeFulfillPacket.time));
                break;
            case PLAYER_STATE:
            case PLAYER_SHOOT:
            case PLAYER_HEALTH:
                final PlayerPacket.TCP playerPacket = (PlayerPacket.TCP) packet;
                queue.add(new PlayerMessage(
                        Message.Type.fromPacketType(playerPacket.type),
                        playerPacket.time,
                        new Vector2f(playerPacket.x, playerPacket.y),
                        new Complexf(playerPacket.c, playerPacket.s),
                        playerPacket.health,
                        playerPacket.playerNumber
                ));
                break;
        }
    }
}
