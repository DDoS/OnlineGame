package ecse414.fall2015.group21.game.CommunicationUtils.Messages;

/**
 * Created by hannes on 9/11/2015.
 *
 * Message Type that is used by the server to respond to request to join messages. This can either be accepted or
 * denied
 */
public class HandshakeMessage extends Message {
    private int clientId;
    private volatile long seed;
    private long accumulatedTime;
    public HandshakeMessage(int messageType, int clientId, long seed, long accumulatedTime) {
        super(messageType);
        this.clientId = clientId;
        this.seed = seed;
        this.accumulatedTime = accumulatedTime;
    }

    public int getClientId() {
        return this.clientId;
    }

    public long getSeed() {
        return this.seed;
    }

    public long getAccumulatedTime() {
        return this.accumulatedTime;
    }
}
