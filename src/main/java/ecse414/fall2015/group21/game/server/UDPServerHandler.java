package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.util.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by hannes on 26/10/2015.
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    UDPServer server;

    UDPServerHandler(UDPServer server) {
        this.server = server;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        System.err.println(packet);
        Message msg = new Message();
        // Phrase message here
        msg.msg = packet.content().toString(CharsetUtil.UTF_8);
        server.addMessage(msg);
        ctx.write(new DatagramPacket(
                Unpooled.copiedBuffer("Some String", CharsetUtil.UTF_8), packet.sender()));
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
