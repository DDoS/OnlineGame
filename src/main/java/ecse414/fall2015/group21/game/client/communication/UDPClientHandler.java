package ecse414.fall2015.group21.game.client.communication;

import ecse414.fall2015.group21.game.CommunicationUtils.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by hannes on 28/10/2015.
 *
 * Handles what the client should do when it receives a message from the server for the UDP client
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    UDPClient client;
    UDPClientHandler(UDPClient client) {
        this.client = client;
    }
    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        System.out.println(packet);
        // Do some Stuff
        Message msg = new Message();
        msg.msg = packet.content().toString(CharsetUtil.UTF_8);
        client.addMessage(msg);
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
