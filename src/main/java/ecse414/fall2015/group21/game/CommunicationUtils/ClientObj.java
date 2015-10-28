package ecse414.fall2015.group21.game.CommunicationUtils;

import java.net.InetSocketAddress;

/**
 * Created by hannes on 28/10/2015.
 *
 * Stores the information about a single client for the server
 */
public class ClientObj {
    private int clientID;
    private InetSocketAddress address;

    ClientObj(int clientID, InetSocketAddress address) {
        this.clientID = clientID;
        this.address = address;
    }

    public InetSocketAddress getSocketAddress() {
        return this.address;
    }

    public int getClientID () {
        return this.clientID;
    }
}
