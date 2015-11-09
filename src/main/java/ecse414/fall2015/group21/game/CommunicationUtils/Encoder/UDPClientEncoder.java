package ecse414.fall2015.group21.game.CommunicationUtils.Encoder;

import ecse414.fall2015.group21.game.CommunicationUtils.Messages.Message;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.MessageType;
import ecse414.fall2015.group21.game.server.universe.Universe;

/**
 * Created by hannes on 9/11/2015.
 *
 * Implementation of the Client Encoder class for a UDP connection
 * This needs to keep track of the unique ID that is assigned to this client
 */
public class UDPClientEncoder extends ClientEncoder {
    private int clientId = 0;

    @Override
    public String encodeUniverse(Universe universe) {
        return "";
    }

    // Encode a request for join message
    @Override
    public String encodeRequestJoin() {
        String msg = "";
        msg = msg + String.format("%2d%", MessageType.REQUEST_CONNECTION);
        return msg;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String encodeAcknowledge() {
        String msg;
        msg = String.format("%2d", MessageType.CONNECTION_ACKNOWLEDGED) + String.format("%2d", clientId);
        return msg;
    }
}
