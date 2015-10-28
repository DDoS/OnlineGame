package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.CommunicationUtils.ClientObj;
import ecse414.fall2015.group21.game.CommunicationUtils.Message;

import java.util.LinkedList;
import java.util.Queue;

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
public class UDPServer extends ServerInterface {
    private int port;
    private LinkedList<ClientObj> connectedClients = new LinkedList<>();
    private Bootstrap b = new Bootstrap();

    UDPServer(int port) {
        // Setup here
        this.port = port;

        EventLoopGroup group = new NioEventLoopGroup();
        try {

            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UDPServerHandler(this));

            b.bind(this.port).sync().channel().closeFuture().await();

        } catch(InterruptedException e){
            System.out.print("Channel was closed");
        } finally {
            group.shutdownGracefully();
        }

    }
    @Override
    public void sendMessage(Message msg) {
        try {
            // Make Message
            String strMsg = msg.toString();
            Channel ch = b.bind(0).sync().channel();

            for (ClientObj client: connectedClients) {
                // Broadcast this message to all clients
                ch.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(strMsg, CharsetUtil.UTF_8),
                        client.getSocketAddress())).sync();
            }



        } catch(InterruptedException e) {
            System.err.println("Channel was interrupted");
        }
    }

    @Override
    public void sendMessageToSingleClient(Message msg, int clientId){
        try {
            // Make Message
            String strMsg = msg.toString();
            Channel ch = b.bind(0).sync().channel();

            // Find client
            int i = 1;
            ClientObj client = connectedClients.getFirst();
            while(client.getClientID() != clientId) {
                client = connectedClients.get(i);
                ++i;
            }

            // Broadcast this message to this client
            ch.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(strMsg, CharsetUtil.UTF_8),
                client.getSocketAddress())).sync();

        } catch(InterruptedException e) {
            System.err.println("Channel was interrupted");
        } catch(ArrayIndexOutOfBoundsException e) {
            System.err.println("Client with Client ID " + clientId + " does not exist");
        }
    }



}
