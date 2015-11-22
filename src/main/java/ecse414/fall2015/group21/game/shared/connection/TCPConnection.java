package ecse414.fall2015.group21.game.shared.connection;

import java.util.LinkedList;
import java.util.Queue;
import io.netty.channel.*;

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
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;


/**
 *  Testing GIT commit to prevent error.
 *  Aidan and Bryce
 */
public class TCPConnection implements Connection {
    private final Address local;
    private final Address remote;
    private final Channel channel;

    public TCPConnection(Address local, Address remote, Channel ch) {
        this.local = local;
        this.remote = remote;
        this.channel = ch;
    }

    @Override
    public Address getAddress() {
        return remote;
    }

    @Override
    public void send(Queue<? extends Message> queue) {
        final Queue<Packet.TCP> encoded = new LinkedList<>();
        queue.forEach(message -> TCPEncoder.INSTANCE.encode(message, local, encoded));
        // TODO: send encoded!

        //not sure if this is even remotely correct...
        channel.connect(local.asInetSocketAddress(),remote.asInetSocketAddress());

       //??? encoded.forEach(message -> channel.write(message));
        //Attempted to emulate what Hannes did, not sure if this is approptiate.
        for(Packet.TCP packet : encoded) {
            try {
                channel.writeAndFlush(
                        new DatagramPacket(
                                packet.asRaw(),
                                new InetSocketAddress(this.remote.asInetAddress(), this.remote.getPort())
                        )).sync();


            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void receive(Queue<? super Message> queue) {
        // TODO: read ByteBufs from channel, use Packet.TCP.FACTORY.newInstance create and add to this queue

        //Is this channel already open? or do we need to open a new channel?


        final Queue<Packet.TCP> received = new LinkedList<>();

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
    }
}
