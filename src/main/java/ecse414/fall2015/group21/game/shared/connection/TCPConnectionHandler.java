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

import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles what a TCP client should do when it receives a message from the server
 */
public class TCPConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {
    // Support concurrency since Netty I/O is asynchronous, or non-blocking
    private final Queue<Packet.TCP> received = new ConcurrentLinkedQueue<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        // Read the incoming message, convert to TCP packet using factory, and add to queue of received packets
        received.add(Packet.Type.TCP_FACTORY.newInstance(msg));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Flush the channel handle context when read is complete
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests
    }

    /**
     * Read all received packets to a queue.
     *
     * @param output the queue where received packets will be put.
     */
    void readPackets(Queue<Packet.TCP> output) {
        while (!received.isEmpty()) {
            output.add(received.poll());
        }
    }
}
