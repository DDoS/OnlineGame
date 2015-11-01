package ecse414.fall2015.group21.game.server.network;

import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The server side of the networking layer
 */
public class ServerNetwork extends TickingElement {
    private final Universe universe;

    public ServerNetwork(Universe universe) {
        super("Network", 20);
        this.universe = universe;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onTick(long dt) {

    }

    @Override
    public void onStop() {

    }
}
