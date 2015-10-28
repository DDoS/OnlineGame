package ecse414.fall2015.group21.game.client.communication;

import ecse414.fall2015.group21.game.CommunicationUtils.ClientObj;
import ecse414.fall2015.group21.game.CommunicationUtils.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Created by hannes on 28/10/2015.
 *
 * Implementation of the ClientInterface class for a UDP client
 */
public class UDPClient extends ClientInterface {
    private int port;
    private InetSocketAddress serverAddress;
    Bootstrap b = new Bootstrap();

    UDPClient(int port, InetSocketAddress serverAddress) {
        this.port = port;
        this.serverAddress = serverAddress;
        EventLoopGroup group = new NioEventLoopGroup();
        try {

            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UDPClientHandler(this));

            Channel ch = b.bind(0).sync().channel();

            // Broadcast the QOTM request to port 8080.
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("Client Message", CharsetUtil.UTF_8),
                    this.serverAddress)).sync();

        } catch(InterruptedException e) {
            System.err.println("Connection Closed");
        }
    }

    @Override
    public void sendMessage(Message msg) {
        try {
            // Make Message
            String strMsg = msg.toString();
            Channel ch = b.bind(0).sync().channel();

            // Broadcast this message to this client
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(strMsg, CharsetUtil.UTF_8),
                    this.serverAddress)).sync();

        } catch(InterruptedException e) {
            System.err.println("Channel was interrupted");
        }
    }
}
