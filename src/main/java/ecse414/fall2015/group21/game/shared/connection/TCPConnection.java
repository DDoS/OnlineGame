package ecse414.fall2015.group21.game.shared.connection;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 *
 */
public class TCPConnection implements Connection {
    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void send(Queue<? extends Message> queue) {

    }

    @Override
    public void receive(Queue<? super Message> queue) {

    }

    @Override
    public void close() {

    }
}
