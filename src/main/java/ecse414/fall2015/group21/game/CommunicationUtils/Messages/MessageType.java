package ecse414.fall2015.group21.game.CommunicationUtils.Messages;

/**
 * Created by hannes on 28/10/2015.
 *
 * Contains all the possible message types that can be exchanged
 */
public class MessageType {
    public static final int UNKOWN_MESSAGE = 0;
    public static final int REQUEST_CONNECTION = 1;
    public static final int CONNECTION_ACCEPTED = 2;
    public static final int CONNECTION_DENIED = 3;
    public static final int CLIENT_STATUS_MESSAGE = 4;
    public static final int SERVER_STATUS_MESSAGE = 5;
    public static final int CONNECTION_ACKNOWLEDGED = 6;

}
