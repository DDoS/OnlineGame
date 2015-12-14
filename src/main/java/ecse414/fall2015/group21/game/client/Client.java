/*
 * This file is part of Online Game, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2015 Group 21
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ecse414.fall2015.group21.game.client;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.Main;
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
        if (!Main.ARGUMENTS.headless) {
            renderer.start();
        }
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
