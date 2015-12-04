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
