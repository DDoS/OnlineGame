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
package ecse414.fall2015.group21.game.shared.data;

/**
 * Interface for a Message, which is used by the game to pass game information.
 *
 */
public interface Message {
    Type getType();

    /**
     * Enumerator for the Types of Messages. Examples are 'CONNECT_REQUEST', 'TIME_REQUEST', and 'PLAYER_STATE'.
     */
    enum Type {
        CONNECT_REQUEST,
        CONNECT_FULFILL,
        TIME_REQUEST,
        TIME_FULFILL,
        PLAYER_STATE,
        PLAYER_SHOOT,
        PLAYER_HEALTH;

        /**
         * Determines the Type of the Message given the Type of a packet.
         *
         * @param packetType    the Type of a packet
         * @return              the Type of this message
         */
        public static Type fromPacketType(Packet.Type packetType) {
            switch (packetType) {
                case CONNECT_REQUEST:
                    return Type.CONNECT_REQUEST;
                case CONNECT_FULFILL:
                    return Type.CONNECT_FULFILL;
                case TIME_REQUEST:
                    return Type.TIME_REQUEST;
                case TIME_FULFILL:
                    return Type.TIME_FULFILL;
                case PLAYER_STATE:
                    return Type.PLAYER_STATE;
                case PLAYER_SHOOT:
                    return Type.PLAYER_SHOOT;
                case PLAYER_HEALTH:
                    return Type.PLAYER_HEALTH;
                default:
                    throw new IllegalArgumentException("Not a packet type: " + packetType.name());
            }
        }
    }
}
