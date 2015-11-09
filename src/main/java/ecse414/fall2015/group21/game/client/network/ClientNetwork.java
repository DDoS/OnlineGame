package ecse414.fall2015.group21.game.client.network;

import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
import ecse414.fall2015.group21.game.util.TickingElement;

/**
 * The client side of the networking layer
 */
public class ClientNetwork extends TickingElement {
    private final RemoteUniverse universe;

    public ClientNetwork(RemoteUniverse universe) {
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
