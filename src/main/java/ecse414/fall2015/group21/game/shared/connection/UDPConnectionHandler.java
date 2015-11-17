package ecse414.fall2015.group21.game.shared.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by hannes on 28/10/2015.
 *
 * Handles what the client should do when it receives a message from the server for the UDP client
 */
public class UDPConnectionHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private UDPConnection client;
    UDPConnectionHandler(UDPConnection client) {
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        ByteBuf msg = packet.content();

        // Demo Message
        // System.out.println(msg);
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
}
