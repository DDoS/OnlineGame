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

import java.util.Map;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.data.Message;

/**
 * Manages connections from server to client.
 */
public interface ConnectionManager {
    /**
     * Initiates the connection manager with a received address for incoming connections.
     *
     * @param receiveAddress The address clients should be connecting to
     */
    void init(Address receiveAddress);

    /**
     * Called periodically to give the manager a chance to read the channel.
     */
    void update();

    /**
     * Opens a connection to a client, given its address and additional optional arguments. Throws an error on failure. Returns the connection if it already exists.
     *
     * @param sendAddress The address of the client
     * @param playerNumber The number of the player this connection is associated to
     * @return The opened connection on success
     */
    Connection openConnection(Address sendAddress, int playerNumber);

    /**
     * Refuses a connection, this is the opposite of {@link #openConnection(Address, int)}. Not necessary for UDP, but needed for TCP. Does nothing if the address never requested a connection.
     *
     * @param sourceAddress The address that sought connection to the server but was refused
     */
    void refuseConnection(Address sourceAddress);

    /**
     * Returns the connection associated to the player number or throws an exception if none exist.
     *
     * @param playerNumber The number of the player the connection is associated to
     * @return A connection if it exists
     */
    Connection getConnection(int playerNumber);

    /**
     * Returns the connection for each player number as a map. Don't modify it!
     *
     * @return The player number to connection map
     */
    Map<Integer, ? extends Connection> getConnections();

    /**
     * Returns whether or not the remote address is connected.
     *
     * @param remote The remote address
     * @return Whether or not it is connected
     */
    boolean isConnected(Address remote);

    /**
     * Closes the connection associated to the player number.
     *
     * @param playerNumber The number of the player the connection is associated to
     */
    void closeConnection(int playerNumber);

    /**
     * Closes all existing connections.
     */
    void closeAll();

    /**
     * Returns messages received that aren't part of a connection. These may be connection requests.
     *
     * @return Messages that aren't part of any connection
     */
    Queue<Message> getUnconnectedMessages();
}
