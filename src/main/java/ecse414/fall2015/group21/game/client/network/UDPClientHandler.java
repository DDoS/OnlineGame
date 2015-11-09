package ecse414.fall2015.group21.game.client.network;


import ecse414.fall2015.group21.game.CommunicationUtils.Decoder.ClientDecoder;
import ecse414.fall2015.group21.game.CommunicationUtils.Decoder.UDPClientDecoder;
import ecse414.fall2015.group21.game.CommunicationUtils.Decoder.UDPServerDecoder;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.HandshakeMessage;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.Message;
import ecse414.fall2015.group21.game.CommunicationUtils.Messages.MessageType;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * Created by hannes on 28/10/2015.
 *
 * Handles what the client should do when it receives a message from the server for the UDP client
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private UDPClientNetwork client;
    private ClientDecoder decoder = new ClientDecoder();
    UDPClientHandler(UDPClientNetwork client) {
        this.client = client;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String msg = packet.content().toString(CharsetUtil.UTF_8);

        // Decode message using Decoder here. If it is a handshake message, deal with it locally, otherwise pass
        // it to the universe
        Message m = decoder.decodeString(msg);

        switch(m.getMessageType()) {
            case(MessageType.CONNECTION_ACCEPTED):
                HandshakeMessage handshakeMessage = (HandshakeMessage) m;
                client.getEncoder().setClientId(handshakeMessage.getClientId());
                // Do something with this?
                handshakeMessage.getSeed();
                handshakeMessage.getAccumulatedTime();
                // Send acc message
                sendMessage(ctx, packet.sender(), client.getEncoder().encodeAcknowledge());
                break;
            case(MessageType.CONNECTION_DENIED):
                System.err.println("Connection Refused");
                break;
            case(MessageType.SERVER_STATUS_MESSAGE):
                break;
            default:
                System.err.println("Received message with unkown message type");
                break;
        }


        // Demo Message
        // System.out.println(msg);
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

    private void sendMessage(ChannelHandlerContext ctx, InetSocketAddress address, String msg) {
        try {
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                    address)).sync();
        } catch (InterruptedException e) {
            System.err.println("Error sending message " + msg + ": " + e.toString());
        }
    }
}
