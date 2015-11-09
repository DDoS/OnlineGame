package ecse414.fall2015.group21.game.CommunicationUtils.Decoder;

import ecse414.fall2015.group21.game.CommunicationUtils.Messages.HandshakeMessage;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.Message;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.MessageType;

/**
 * Created by hannes on 9/11/2015.
 *
 * Implementation of the decoder for the Client
 */
public class ClientDecoder extends Decoder {
    @Override
    public Message decodeString(String msg) {
        Message decodedMessage;
        int msg_type = Integer.parseInt(msg.substring(0, 1));
        switch(msg_type) {
            case(MessageType.CONNECTION_DENIED):
                decodedMessage = new HandshakeMessage(MessageType.CONNECTION_DENIED, 0, 0, 0);
                break;
            case(MessageType.CONNECTION_ACCEPTED):
                int clientId = Integer.parseInt(msg.substring(2, 3));
                long seed = Long.parseLong(msg.substring(4, 23));
                long accumulatedTime = Long.parseLong(msg.substring(24, 43));
                decodedMessage = new HandshakeMessage(MessageType.CONNECTION_ACCEPTED, clientId, seed, accumulatedTime);
                break;
            case(MessageType.SERVER_STATUS_MESSAGE):
                decodedMessage = new Message(MessageType.SERVER_STATUS_MESSAGE);
                break;
            default:
                decodedMessage = new Message(MessageType.UNKOWN_MESSAGE);
                break;
        }
        return decodedMessage;
    }
}
