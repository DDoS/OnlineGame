package ecse414.fall2015.group21.game.shared.connection;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Queue;

import ecse414.fall2015.group21.game.shared.codec.TCPDecoder;
import ecse414.fall2015.group21.game.shared.codec.TCPEncoder;
import ecse414.fall2015.group21.game.shared.data.Message;
import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.channel.Channel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;


/**
 *  Represents a TCP connection. It holds the addresses (IP and port) of both sides of the connection.
 *  Responsible for reading and sending TCP packets
 *
 *  Aidan and Bryce
 */
public class TCPConnection implements Connection {
    private Address local;
    private final Address remote;
    private final Channel channel;
    private final Queue<Packet.TCP> received = new LinkedList<>();

    private boolean managed;
    private EventLoopGroup group = null;
    private TCPConnectionHandler handler = null;

    /**
     * Creates a new TCP connection. No manager is needed for this single connection.
     *
     * @param local The local address of the connection (port)
     * @param remote The remote address to send to (ip and port)
     */
    public TCPConnection(Address local, Address remote){
        this.local = local;
        this.remote = remote;
        group = new NioEventLoopGroup();    //allows channels to be processed
        handler = new TCPConnectionHandler();

        //Attempt to create a new channel, throws exception on failure
        try{
            Bootstrap startUp = new Bootstrap();

            //Bootstrap documentation says to use connect() methods instead of .bind() for TCP
            channel = startUp.group(group)
                    .channel(NioSocketChannel.class)    //Netty NIO socket channel
                    .handler(handler)
                    .localAddress(local.getPort())                      //Add local port to the channel
                    .connect(remote.asInetAddress(), remote.getPort())  //connect the remote Address to the channel
                    .channel()
                    ;


        } catch (Exception exception) {
            group.shutdownGracefully();
            throw new RuntimeException("Failed to create connection at " + local, exception);
        }

        System.out.println("TCP Connection opened at " + local.toString());
        managed = false;    //this connection has no manager
    }

    /**
     * Creates a TCP connection from an existing Channel.
     * This connection will use the channel to send but will not read the channel.
     * Reading the channel is handled by TCP connection manager by calling handOff(Packet.TCP)
     *
     * @param local The local address of the connection (port)
     * @param remote The remove address to be sent to (ip and port)
     * @param ch The existing Channel used for sending
     * @param handler The existing handler for this channel
     */
    public TCPConnection(Address local, Address remote, Channel ch, TCPConnectionHandler handler) {
        this.local = local;
        this.remote = remote;
        this.channel = ch;
        this.handler = handler;
        managed = true;
    }

    @Override
    public Address getRemote() {
        return remote;
    }


    @Override
    public Address getLocal() {
        return local;
    }

    @Override
    public void setLocal(Address local) {
        this.local = local;
    }

    /**
     * Send all messages
     *
     * Not finished
     *
     * @param queue The messages to send
     */
    @Override
    public void send(Queue<? extends Message> queue) {
        final Queue<Packet.TCP> encoded = new LinkedList<>();
        queue.forEach(message -> TCPEncoder.INSTANCE.encode(message, local, remote, encoded));
        // For each packet in the queue, we want to send it over the channel that we are given
        encoded.forEach(packet -> channel.writeAndFlush(packet.asRaw()));
    }

    @Override
    public void receive(Queue<? super Message> queue) {
        if (queue.isEmpty()) {
            return;
        }

        handler.readPackets(received);

        //Decode packets using factory and add them to the queue
        for (Packet.TCP packet : received) {
            TCPDecoder.INSTANCE.decode(packet, remote, queue);
        }
    }

    @Override
    public void close() {
        // TODO: close connection if still open

        //Attempting to close connection...
        if (channel.isActive()){
            channel.close();

        }
        if(group != null) {
            group.shutdownGracefully();
        }
    }

    //HandOff method for TCP connection manager to read the channel
    void handOff(Packet.TCP received) {
        this.received.add(received);
    }
}
