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
package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.server.console.Console;
import ecse414.fall2015.group21.game.server.network.ServerNetwork;
import ecse414.fall2015.group21.game.server.universe.Universe;

/**
 * Represents the client and is used to manage all its threads.
 */
public class Server extends Game {
    private final Console console;
    private final Universe universe;
    private final ServerNetwork network;

    public Server() {
        this.console = new Console(System.in, System.out, this::close);
        this.universe = new Universe();
        this.network = new ServerNetwork(universe);
    }

    @Override
    protected void start() {
        console.start();
        universe.start();
        network.start();
    }

    @Override
    protected void stop() {
        network.stop();
        universe.stop();
        console.stop();
    }
}
