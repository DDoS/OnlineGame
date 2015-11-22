package ecse414.fall2015.group21.game.shared.connection;

import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hannes on 28/10/2015.
 *
 * Handles what the client should do when it receives a message from the server for the UDP client
 */
public class UDPConnectionHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private final Queue<Packet.UDP> received = new LinkedList<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        // Extract the sent ByteBuf from the UDP packet
        ByteBuf msg = packet.content();

        // Add this to the received packet queue
        received.add(Packet.UDP.FACTORY.newInstance(msg));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests.
    }

    protected void readPackets(Queue<Packet.UDP> queue) {
        queue.addAll(received);

        // We need to empty the queue so that packets aren't read multiple times
        received.clear();
    }
}
