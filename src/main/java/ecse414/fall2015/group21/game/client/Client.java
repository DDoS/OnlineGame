package ecse414.fall2015.group21.game.client;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.render.Renderer;

/**
 * Represents the client and is used to manage all its threads.
 */
public class Client extends Game {
    private final Renderer renderer;
    private final Input input;

    /**
     * Instantiates a new game with a world, input and renderer.
     */
    public Client() {
        renderer = new Renderer(this);
        input = new Input(this);
    }

    @Override
    protected void start() {
        super.start();
        renderer.start();
        input.start();
    }

    @Override
    protected void stop() {
        input.stop();
        renderer.stop();
        super.stop();
    }

    /**
     * Gets the interface.
     *
     * @return the interface
     */
    public Renderer getInterface() {
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
