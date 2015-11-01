package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.server.console.Console;
import ecse414.fall2015.group21.game.server.universe.Universe;

/**
 * Represents the client and is used to manage all its threads.
 */
public class Server extends Game {
    private final Console console;
    private final Universe universe;

    public Server() {
        this.console = new Console(System.in, System.out, this::close);
        this.universe = new Universe();
    }

    @Override
    protected void start() {
        console.start();
        universe.start();
    }

    @Override
    protected void stop() {
        universe.stop();
        console.stop();
    }
}
