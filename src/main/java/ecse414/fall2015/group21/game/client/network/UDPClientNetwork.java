package ecse414.fall2015.group21.game.client.network;

import ecse414.fall2015.group21.game.CommunicationUtils.Encoder.UDPClientEncoder;
import ecse414.fall2015.group21.game.CommunicationUtils.Encoder.UDPServerEncoder;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
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
 * Created by hannes on 9/11/2015.
 *
 * Subclass implementation of ClientNetwork for a UDP Connection
 */
public class UDPClientNetwork extends ClientNetwork {
    private InetSocketAddress serverAddress;
    private UDPClientEncoder encoder = new UDPClientEncoder();
    private Bootstrap b = new Bootstrap();
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel ch;

    public UDPClientNetwork(RemoteUniverse universe, InetSocketAddress serverAddress) {
        super(universe);
        this.serverAddress = serverAddress;
    }

    @Override
    public void onStart() {
        // Setup the client networking handler
        try {
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UDPClientHandler(this));

            ch = b.bind(0).sync().channel();
        } catch (InterruptedException e) {
            System.err.println("Error setting up client communication: " + e.toString());
        }
        // Send request to join to game
        String msg = encoder.encodeRequestJoin();
        // Get some timer here
        sendMessage(msg);
    }

    @Override
    public void onTick(long dt) {
        // Demo Message
        String strMsg = "Test";

        // Call the encoder here to encode the remoteuniverse and then send it
        // String strMsg = Encoder(Universe);

        sendMessage(strMsg);
    }

    @Override
    public void onStop() {
        group.shutdownGracefully();
    }

    private void sendMessage(String msg) {
        try {
            // Broadcast this message to this client
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                    this.serverAddress)).sync();
        } catch (InterruptedException e) {
            System.err.println("Error sending message to server: " + e.toString());
        }
    }

    public UDPClientEncoder getEncoder() {
        return this.encoder;
    }
}
