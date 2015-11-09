package ecse414.fall2015.group21.game.CommunicationUtils.Encoder;


import ecse414.fall2015.group21.game.CommunicationUtils.Messages.HandshakeMessage;

/**
 * Created by hannes on 9/11/2015.
 *
 * Implementation of the Encoder Class
 * It will contain several additional functions that only a client will need
 * Functions will be implemented in the specific UDP or TCP implementation
 */
public class ServerEncoder extends Encoder {
    public String encodeHandshakeResponse(HandshakeMessage m) {
        return "";
    }
}
