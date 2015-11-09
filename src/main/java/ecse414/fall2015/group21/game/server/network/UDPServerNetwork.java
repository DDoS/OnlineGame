package ecse414.fall2015.group21.game.server.network;

import ecse414.fall2015.group21.game.CommunicationUtils.ClientObj;

import java.util.LinkedList;

import ecse414.fall2015.group21.game.CommunicationUtils.Encoder.UDPServerEncoder;
import ecse414.fall2015.group21.game.server.universe.Universe;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by hannes on 26/10/2015.
 *
 * Implementation of ServerInterface for a UDP server. Handles setting up the connection and sending messages
 */
public class UDPServerNetwork extends ServerNetwork {
    private int port;
    private LinkedList<ClientObj> connectedClients = new LinkedList<>();
    private Bootstrap b = new Bootstrap();
    private UDPServerEncoder encoder = new UDPServerEncoder();
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel ch;

    public UDPServerNetwork(Universe universe, int port) {
        super(universe);
        this.port = port;
    }

    @Override
    public void onStart() {
        try {
            // Setup the Channel Handler
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UDPServerHandler(this));

            ch = b.bind(this.port).sync().channel();

        } catch(InterruptedException e){
            System.out.print("Failed to setup server: " +  e.toString());
        }
    }

    @Override
    public void onTick(long dt) {
        // Demo Message
        String strMsg = "Test from server";

        // Call encoder and send message to client as string
        // String strMsg = Encoder.encode(universe);

        // Send this message to all connected clients
        for (ClientObj client : connectedClients) {
            try {
                ch.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(strMsg, CharsetUtil.UTF_8),
                        client.getSocketAddress())).sync();

            } catch (InterruptedException e) {
                System.err.println("Failed to send message to client " + client.getClientID() + ": " + e.toString());
            }
        }
    }

    @Override
    public void onStop() {
        group.shutdownGracefully();
    }

    // Add client to the Connected client list
    public void addClient(ClientObj client) {
        connectedClients.add(client);
    }

    public UDPServerEncoder getEncoder() {
        return encoder;
    }
}
