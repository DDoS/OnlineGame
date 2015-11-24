package ecse414.fall2015.group21.game.shared.connection;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by hannes on 25/11/2015.
 *
 * Class that initialises a new channel for a TCP connection. This will be used by the server to set up all the
 * connection handlers properly
 */
public class TCPConnectionInitializer extends ChannelInitializer<SocketChannel> {
    private final Queue<Pending> pendingConnections = new ConcurrentLinkedQueue<>();

    private class Pending {
        Address address;
        SocketChannel ch;
        TCPConnectionHandler handler;
        protected Pending(Address address, SocketChannel ch, TCPConnectionHandler handler) {
            this.address = address;
            this.ch = ch;
            this.handler = handler;
        }
    }

    protected void getPendingMessages(Queue<Message> queue) {
        for(Pending client : pendingConnections) {
            // Get All messages from the handler queues
            Queue<Packet.TCP> received = new LinkedList<>();
            client.handler.readPackets(received);
            // If we have connection request packets, add these to the connection message queue
            for(Packet.TCP packet : received) {
                if(packet instanceof ConnectRequestPacket) {
                    TCPDecoder.INSTANCE.decode(packet, client.address, queue);
                }
            }
        }
    }

    protected void removePending(Address client) {
        for(Pending list : pendingConnections) {
            if(list.address.equals(client)) {
                pendingConnections.remove(list);
            }
        }
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a new handler
        TCPConnectionHandler handler = new TCPConnectionHandler();
        // Setup the pipeline for the new socket
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(handler);

        // Add the new connection to the pendingConnections map
        int ip = 0;
        byte bytes[] = ch.remoteAddress().getAddress().getAddress();
        for (int i = 0; i < bytes.length; i++) {
            ip <<= 8;
            ip |= bytes[i] & 0xff;
        }
        Address sender = Address.forUnconnectedRemoteClient(ip, (short) ch.remoteAddress().getPort());
        Pending newConnection = new Pending(sender, ch, handler);
        pendingConnections.add(newConnection);
    }
}
