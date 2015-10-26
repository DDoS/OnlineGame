package ecse414.fall2015.group21.game.server;

import ecse414.fall2015.group21.game.util.Message;

import java.util.LinkedList;
import java.util.Queue;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by hannes on 26/10/2015.
 */
public class UDPServer extends ServerInterface {
    private int port;
    private Queue<Message> messageQueue = new LinkedList<Message>();
    private LinkedList<ClientObj> connectedClients = new LinkedList<ClientObj>();

    UDPServer(int port) {
        // Setup here
        this.port = port;

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new UDPServerHandler(this));

            b.bind(this.port).sync().channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }
    @Override
    public void sendMessage(Message msg) {

    }

    @Override
    public Message getMessage() {
        return messageQueue.remove();
    }

    public void addMessage(Message msg) {
        messageQueue.add(msg);
    }
}
