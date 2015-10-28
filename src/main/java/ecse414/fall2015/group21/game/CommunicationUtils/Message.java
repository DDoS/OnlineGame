package ecse414.fall2015.group21.game.CommunicationUtils;

/**
 * Created by hannes on 26/10/2015.
 *
 * Superclass that implements a message that will be sent between the server and the client
 */
public class Message {
    // Example message
    public String msg;

    @Override
    public String toString() {
        return this.msg;
    }
}
