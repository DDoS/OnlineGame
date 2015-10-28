package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.CommunicationUtils.Message;

import java.util.LinkedList;
import java.util.Queue;

public class ServerInterface {
    private Queue<Message> messageQueue = new LinkedList<>();

    public void sendMessage(Message msg) {

    }

    public void sendMessageToSingleClient(Message msg, int clientId) {

    }

    public Message getMessage() {
        return messageQueue.remove();
    }

    public void addMessage(Message msg) {
        messageQueue.add(msg);
    }
}
