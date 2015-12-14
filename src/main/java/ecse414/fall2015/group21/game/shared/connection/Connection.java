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
package ecse414.fall2015.group21.game.shared.connection;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 * A full duplex connection between a client and server. Not bound to a particular side.
 */
public interface Connection {
    /**
     * Returns the address on the opposite side of the connection.
     *
     * @return The opposite address
     */
    Address getRemote();

    /**
     * Gets the local address.
     *
     * @return The local address
     */
    Address getLocal();

    /**
     * Sets the local address.
     *
     * @param local The local address
     */
    void setLocal(Address local);

    /**
     * Sends to the opposite entity.
     *
     * @param queue The messages to send
     */
    void send(Queue<? extends Message> queue);

    /**
     * Read what was received from the opposite entity.
     *
     * @param queue The messages that were received
     */
    void receive(Queue<? super Message> queue);

    /**
     * Closes the connection.
     */
    void close();
}
