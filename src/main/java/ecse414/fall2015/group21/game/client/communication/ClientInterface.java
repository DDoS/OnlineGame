package ecse414.fall2015.group21.game.client.communication;

import ecse414.fall2015.group21.game.CommunicationUtils.Message;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hannes on 28/10/2015.
 *
 * Superclass that implements the main features that the client program will have access to
 */
public class ClientInterface {
    private Queue<Message> messageQueue = new LinkedList<>();

    public void sendMessage(Message msg) {

    }

    public Message getMessage() {
        return messageQueue.remove();
    }

    public void addMessage(Message msg) {
        messageQueue.add(msg);
    }
}
