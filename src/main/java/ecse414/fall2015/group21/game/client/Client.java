package ecse414.fall2015.group21.game.client;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.network.ClientNetwork;
import ecse414.fall2015.group21.game.client.render.Renderer;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;

/**
 * Represents the client and is used to manage all its threads.
 */
public class Client extends Game {
    private final Input input;
    private final RemoteUniverse universe;
    private final ClientNetwork network;
    private final Renderer renderer;

    /**
     * Instantiates a new game with a remote universe, input and renderer.
     */
    public Client() {
        input = new Input(this::close);
        universe = new RemoteUniverse(input);
        network = new ClientNetwork(universe);
        renderer = new Renderer(input, universe);
    }

    @Override
    protected void start() {
        input.start();
        universe.start();
        network.start();
        renderer.start();
    }

    @Override
    protected void stop() {
        renderer.stop();
        network.stop();
        universe.stop();
        input.stop();
    }

    /**
     * Gets the remote universe.
     *
     * @return the remote universe
     */
    public RemoteUniverse getUniverse() {
        return universe;
    }

    /**
     * Gets the client network.
     *
     * @return the client network
     */
    public ClientNetwork getNetwork() {
        return network;
    }

    /**
     * Gets the renderer.
     *
     * @return the renderer
     */
    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Gets the input.
     *
     * @return the input
     */
    public Input getInput() {
        return input;
    }
}
