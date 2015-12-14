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
import java.util.concurrent.ConcurrentLinkedQueue;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Handles what the client should do when it receives a message from the server for the UDP client.
 */
public class UDPConnectionHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    // Netty I/O is asynchronous, so use a collection that can support concurrency
    private final Queue<DatagramPacket> received = new ConcurrentLinkedQueue<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        // We're going to add a reference to the packet to the queue, so increment the counter
        packet.retain();
        // Add to the received packet queue to be processed later
        received.add(packet);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests
    }

    /**
     * Places recieved packets in the queue.
     *
     * @param queue the queue where recieved packets will be placed.
     */
    void readPackets(Queue<DatagramPacket> queue) {
        // Transfer the elements to the given queue
        // Using addAll() and clear() could cause packets received between the calls to be missed
        while (!received.isEmpty()) {
            queue.add(received.poll());
        }
    }
}
