package ecse414.fall2015.group21.game.shared.connection;


import ecse414.fall2015.group21.game.shared.data.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles what a TCP client should do when it recieves a message from the server
 */
public class TCPConnectionHandler extends SimpleChannelInboundHandler<DatagramPacket>{
    //Support concurrency since Netty I/O is asynchronous, or non-blocking
    private final Queue<Packet.TCP> received = new ConcurrentLinkedQueue<>();



    /**
     * Is called for each message of type I
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        // Read the incoming message, convert to TCP packet using factory, and add to queue of recieved packets
        Packet.TCP tcpPacket = Packet.TCP.FACTORY.newInstance(msg.content());
        received.add(tcpPacket);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        //Flush the channel handle context when read is complete
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        // We don't close the channel because we can keep serving requests - is this still true for TCP?
    }

    //Read all recieved packets to a queue
    public void readPackets(Queue<Packet.TCP> output){
        while(!received.isEmpty()){
            output.add(received.poll());
        }

    }

}
