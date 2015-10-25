package ecse414.fall2015.group21.game.client;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.render.Renderer;
import ecse414.fall2015.group21.game.client.universe.Universe;

/**
 * Represents the client and is used to manage all its threads.
 */
public class Client extends Game {
    private final Universe universe;
    private final Renderer renderer;
    private final Input input;

    /**
     * Instantiates a new game with a universe, input and renderer.
     */
    public Client() {
        universe = new Universe(this);
        renderer = new Renderer(this);
        input = new Input(this);
    }

    @Override
    protected void start() {
        universe.start();
        renderer.start();
        input.start();
    }

    @Override
    protected void stop() {
        input.stop();
        renderer.stop();
        universe.stop();
    }

    /**
     * Gets the universe.
     *
     * @return the universe
     */
    public Universe getUniverse() {
        return universe;
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
