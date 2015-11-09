package ecse414.fall2015.group21.game.CommunicationUtils.Decoder;

import ecse414.fall2015.group21.game.CommunicationUtils.Messages.Message;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.MessageType;

/**
 * Created by hannes on 9/11/2015.
 *
 * Implements the decoder on the server side. Messages that are relevant are:
 * - Request Connection
 * - Client Status Message
 * - Connection Acknowledge
 */
public class ServerDecoder extends Decoder {
    @Override
    public Message decodeString(String msg) {
        Message decodedMessage;
        int msg_type = Integer.parseInt(msg.substring(0,1));
        switch(msg_type) {
            case(MessageType.REQUEST_CONNECTION):
                decodedMessage = new Message(MessageType.REQUEST_CONNECTION);
                break;
            case(MessageType.CLIENT_STATUS_MESSAGE):
                decodedMessage = new Message(MessageType.CLIENT_STATUS_MESSAGE);
                break;
            default:
                decodedMessage = new Message(MessageType.UNKOWN_MESSAGE);
                break;
        }
        return decodedMessage;
    }
}
