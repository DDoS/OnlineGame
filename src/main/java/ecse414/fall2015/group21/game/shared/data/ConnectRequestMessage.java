package ecse414.fall2015.group21.game.shared.data;

import ecse414.fall2015.group21.game.shared.connection.Address;

/**
 * ConnectRequestMessage is a message type used to request a connection to the server.
 */
public class ConnectRequestMessage implements Message {
    public final Address address;

    public ConnectRequestMessage(Address address) {
        this.address = address;
    }

    @Override
    public Type getType() {
        return Type.CONNECT_REQUEST;
    }
}
