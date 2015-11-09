package ecse414.fall2015.group21.game.CommunicationUtils.Messages;

/**
 * Created by hannes on 26/10/2015.
 *
 * Superclass that implements a message that will be sent between the server and the client
 */
public class Message {
    private int messageType;

    public Message(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return messageType;
    }
}
