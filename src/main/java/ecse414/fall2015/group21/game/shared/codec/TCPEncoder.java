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
package ecse414.fall2015.group21.game.shared.codec;

import java.util.Queue;

import ecse414.fall2015.group21.game.shared.connection.Address;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerMessage;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillMessage;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestMessage;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;

/**
 *  Encodes messages into TCP packets and places them in a queue.
 */
public final class TCPEncoder implements Encoder<Packet.TCP> {
    public static final TCPEncoder INSTANCE = new TCPEncoder();

    private TCPEncoder() {
    }

    @Override
    public void encode(Message message, Address source, Address destination, Queue<? super Packet.TCP> queue) {
        switch (message.getType()) {
            case CONNECT_REQUEST:
                queue.add(new ConnectRequestPacket.TCP());
                break;
            case CONNECT_FULFILL:
                final ConnectFulfillMessage connectFulfillMessage = (ConnectFulfillMessage) message;
                queue.add(new ConnectFulfillPacket.TCP(connectFulfillMessage.playerNumber, connectFulfillMessage.seed, connectFulfillMessage.time));
                break;
            case TIME_REQUEST:
                final TimeRequestMessage timeRequestMessage = (TimeRequestMessage) message;
                queue.add(new TimeRequestPacket.TCP(timeRequestMessage.requestNumber));
                break;
            case TIME_FULFILL:
                final TimeFulfillMessage timeFulfillMessage = (TimeFulfillMessage) message;
                queue.add(new TimeFulfillPacket.TCP(timeFulfillMessage.requestNumber, timeFulfillMessage.time));
                break;
            case PLAYER_STATE:
            case PLAYER_SHOOT:
            case PLAYER_HEALTH:
                final PlayerMessage playerMessage = (PlayerMessage) message;
                queue.add(new PlayerPacket.TCP(
                        Packet.Type.fromMessageType(playerMessage.type),
                        playerMessage.time,
                        playerMessage.position.getX(), playerMessage.position.getY(),
                        playerMessage.rotation.getX(), playerMessage.rotation.getY(),
                        (short) playerMessage.playerNumber,
                        playerMessage.health
                ));
                break;
        }
    }
}
