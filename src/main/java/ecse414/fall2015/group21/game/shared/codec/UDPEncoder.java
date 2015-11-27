package ecse414.fall2015.group21.game.shared.codec;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;

/**
 *
 */
public final class UDPEncoder implements Encoder<Packet.UDP> {
    public static final UDPEncoder INSTANCE = new UDPEncoder();

    private UDPEncoder() {
    }

    @Override
    public void encode(Message message, Address source, Address destination, Queue<? super Packet.UDP> queue) {
        switch (message.getType()) {
            case CONNECT_REQUEST:
                queue.add(new ConnectRequestPacket.UDP());
                break;
            case CONNECT_FULFILL:
                final ConnectFulfillMessage connectFulfillMessage = (ConnectFulfillMessage) message;
                queue.add(new ConnectFulfillPacket.UDP(connectFulfillMessage.playerNumber, connectFulfillMessage.seed, connectFulfillMessage.time, destination.getSharedSecret()));
                break;
            case TIME_REQUEST:
                final TimeRequestMessage timeRequestMessage = (TimeRequestMessage) message;
                queue.add(new TimeRequestPacket.UDP(timeRequestMessage.requestNumber, source.getSharedSecret()));
                break;
            case TIME_FULFILL:
                final TimeFulfillMessage timeFulfillMessage = (TimeFulfillMessage) message;
                queue.add(new TimeFulfillPacket.UDP(timeFulfillMessage.requestNumber, timeFulfillMessage.time));
                break;
            case PLAYER_STATE:
            case PLAYER_SHOOT:
            case PLAYER_HEALTH:
                final PlayerMessage playerMessage = (PlayerMessage) message;
                queue.add(new PlayerPacket.UDP(
                        Packet.Type.fromMessageType(playerMessage.type),
                        playerMessage.time,
                        playerMessage.position.getX(), playerMessage.position.getY(),
                        playerMessage.rotation.getX(), playerMessage.rotation.getY(),
                        (short) playerMessage.playerNumber,
                        playerMessage.health,
                        source.getSharedSecret()
                ));
                break;
        }
    }
}
