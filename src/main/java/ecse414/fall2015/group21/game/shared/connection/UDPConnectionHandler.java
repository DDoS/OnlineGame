package ecse414.fall2015.group21.game.shared.connection;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Handles what the client should do when it receives a message from the server for the UDP client.
 */
public class UDPConnectionHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    // Netty I/O is asynchronous, so use a collection that can support concurrency
    private final Queue<Packet.UDP> received = new ConcurrentLinkedQueue<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        // Extract the sent bytes, convert to a UDP packet and add to the received packet queue
        received.add(Packet.UDP.FACTORY.newInstance(packet.content()));
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

    void readPackets(Queue<Packet.UDP> queue) {
        // Transfer the elements to the given queue
        // Using addAll() and clear() could cause packets received between the calls to be missed
        while (!received.isEmpty()) {
            queue.add(received.poll());
        }
    }
}
