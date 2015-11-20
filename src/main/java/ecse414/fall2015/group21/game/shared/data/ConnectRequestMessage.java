package ecse414.fall2015.group21.game.shared.data;

/**
 *
 */
public class ConnectRequestMessage implements Message {
    public final int ipAddress;
    public final short port;

    public ConnectRequestMessage(int ipAddress, short port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_REQUEST;
    }
}
