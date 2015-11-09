package ecse414.fall2015.group21.game.CommunicationUtils.Encoder;


import ecse414.fall2015.group21.game.CommunicationUtils.Messages.HandshakeMessage;
import ecse414.fall2015.group21.game.server.universe.Universe;

/**
 * Created by hannes on 6/11/2015.
 *
 * Implementation of the ServerEncoder class for a UDP connection
 */
public class UDPServerEncoder extends ServerEncoder {

    @Override
    public String encodeUniverse(Universe universe) {
        return "";
    }

    @Override
    public String encodeHandshakeResponse(HandshakeMessage m) {
        String msg;
        String msgType = String.format("%2d", m.getMessageType());
        String clientId = String.format("%2d", m.getClientId());
        String seed = String.format("%20d", m.getSeed());
        String accumulated = String.format("%20d", m.getAccumulatedTime());
        msg = msgType + clientId + seed + accumulated;
        return msg;
    }
}
