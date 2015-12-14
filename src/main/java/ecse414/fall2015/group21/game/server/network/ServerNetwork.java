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
package ecse414.fall2015.group21.game.server.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import ecse414.fall2015.group21.game.Main;
import ecse414.fall2015.group21.game.client.network.ClientNetwork;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.shared.connection.Connection;
import ecse414.fall2015.group21.game.shared.connection.ConnectionManager;
import ecse414.fall2015.group21.game.shared.connection.TCPConnectionManager;
import ecse414.fall2015.group21.game.shared.connection.UDPConnectionManager;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestMessage;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.util.TickingElement;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * The server side of the networking layer
 */
public class ServerNetwork extends TickingElement {
    private static final int PLAYER_LIMIT = 15;
    private final Universe universe;
    private ConnectionManager connections;
    private final Map<Integer, Long> lastEventTimes = new HashMap<>();
    private int playerCount = 0;
    private int playerIndex = 0;

    public ServerNetwork(Universe universe) {
        super("ServerNetwork", 60);
        this.universe = universe;
    }

    @Override
    public void onStart() {
        switch (Main.ARGUMENTS.type) {
            case "udp":
                connections = new UDPConnectionManager();
                break;
            case "tcp":
                connections = new TCPConnectionManager();
                break;
            default:
                throw new UnsupportedOperationException(Main.ARGUMENTS.type);
        }
        System.out.println("Using network protocol: " + Main.ARGUMENTS.type);
        connections.init(Main.ARGUMENTS.address());
    }

    @Override
    public void onTick(long dt) {
        final Queue<Message> reusedQueue = new LinkedList<>();
        // Perform some internal update of connections
        connections.update();
        // Process unconnected messages
        final Queue<Message> unconnectedMessages = connections.getUnconnectedMessages();
        while (!unconnectedMessages.isEmpty()) {
            final Message message = unconnectedMessages.poll();
            // Process connection requests only
            if (message.getType() == Message.Type.CONNECT_REQUEST) {
                reusedQueue.clear();
                processConnectRequest((ConnectRequestMessage) message, reusedQueue);
            }
        }
        // Process messages from each connection
        final Queue<Integer> timeouts = new LinkedList<>();
        final Map<Integer, Player> players = universe.getPlayers();
        final Queue<Message> events = pollEvents();
        for (Map.Entry<Integer, ? extends Connection> entry : connections.getConnections().entrySet()) {
            final Integer playerNumber = entry.getKey();
            // First check that the connection is still alive
            if (timedOut(playerNumber)) {
                timeouts.add(playerNumber);
                break;
            }
            // Get the connection, read the messages
            final Connection connection = entry.getValue();
            reusedQueue.clear();
            connection.receive(reusedQueue);
            // Process them
            processPlayerMessages(playerNumber, connection, reusedQueue);
            reusedQueue.clear();
            // Send player states and events
            players.forEach((number, player) -> reusedQueue.add(new PlayerMessage(Message.Type.PLAYER_STATE, player, false)));
            events.forEach(reusedQueue::add);
            connection.send(reusedQueue);
        }
        // Disconnect timeouts
        timeouts.forEach((playerNumber) -> {
            connections.closeConnection(playerNumber);
            lastEventTimes.remove(playerNumber);
            playerCount--;
            // Kill player
            universe.handOff(new PlayerMessage(Message.Type.PLAYER_HEALTH, 0, Vector2f.ZERO, Complexf.IDENTITY, (short) 0, playerNumber));
            System.out.println("Connection timed out for player " + playerNumber);
        });
    }

    private void processConnectRequest(ConnectRequestMessage message, Queue<Message> reusedQueue) {
        // Connect is not already connected and we have room
        if (playerCount < PLAYER_LIMIT && !connections.isConnected(message.address)) {
            // Open the connection, reply with a fulfill message
            final int playerNumber = playerIndex++;
            final Connection connection = connections.openConnection(message.address, playerNumber);
            final ConnectFulfillMessage connectFulfill = new ConnectFulfillMessage((short) playerNumber, universe.getSeed(), universe.getTime());
            reusedQueue.add(connectFulfill);
            connection.send(reusedQueue);
            // Also pass to universe to add player
            universe.handOff(connectFulfill);
            lastEventTimes.put(playerNumber, System.nanoTime());
            playerCount++;
            System.out.println("Connected " + message.address + " as player " + playerNumber);
        } else {
            // Refuse connection, allow TCP to close it
            connections.refuseConnection(message.address);
            System.out.println("Refused " + message.address);
        }
    }

    private boolean timedOut(int playerNumber) {
        final long currentEventTime = System.nanoTime();
        final long lastEventTime = lastEventTimes.get(playerNumber);
        // Check time elapsed since last connect/time request
        return currentEventTime - lastEventTime > ClientNetwork.TIME_REQUEST_PERIOD * 2;
    }

    private void processPlayerMessages(int playerNumber, Connection connection, Queue<Message> messages) {
        final Queue<Message> toSend = new LinkedList<>();
        // Create a list of replies for the received messages
        while (!messages.isEmpty()) {
            final Message message = messages.poll();
            switch (message.getType()) {
                case TIME_REQUEST:
                    // Send a fulfill with the time as the reply
                    final TimeRequestMessage timeRequest = (TimeRequestMessage) message;
                    toSend.add(new TimeFulfillMessage(timeRequest.requestNumber, universe.getTime()));
                    lastEventTimes.put(playerNumber, System.nanoTime());
                    break;
                case PLAYER_STATE:
                case PLAYER_SHOOT:
                case PLAYER_HEALTH:
                    // Move to universe
                    universe.handOff(message);
                    break;
                default:
                    // Not a message the server should care about
                    break;
            }
        }
        // Send all out replies
        connection.send(toSend);
    }

    private Queue<Message> pollEvents() {
        final Queue<Message> polled = new LinkedList<>();
        final Queue<Message> events = universe.getEvents();
        while (!events.isEmpty()) {
            polled.add(events.poll());
        }
        return polled;
    }

    @Override
    public void onStop() {
        connections.closeAll();
        connections = null;
    }

    public Universe getUniverse() {
        return this.universe;
    }
}
