package ecse414.fall2015.group21.game.server.network;

import ecse414.fall2015.group21.game.CommunicationUtils.Decoder.ServerDecoder;
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
 * Created by hannes on 26/10/2015.
 *
 * Handles what the server should do when it receives a message from the client for the UDP server
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private int noClients;
    private UDPServerNetwork server;
    private ServerDecoder decoder = new ServerDecoder();

    UDPServerHandler(UDPServerNetwork server) {
        this.server = server;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        String msg = packet.content().toString(CharsetUtil.UTF_8);

        // Decode message using Decoder here. If it is a handshake message, deal with it locally, otherwise pass
        // it to the universe
        Message m = decoder.decodeString(msg);

        switch(m.getMessageType()) {
            case(MessageType.REQUEST_CONNECTION):
                HandshakeMessage returnMsg;
                if(noClients < 10) {
                    // Get random client ID
                    int clientId = 999;
                    returnMsg = new HandshakeMessage(MessageType.CONNECTION_ACCEPTED, clientId,
                            server.getUniverse().getSeed(), server.getUniverse().getTime());
                } else {
                    returnMsg = new HandshakeMessage(MessageType.CONNECTION_DENIED, 0, 0, 0);
                }
                sendMessage(ctx, packet.sender(), server.getEncoder().encodeHandshakeResponse(returnMsg));
                break;
            case(MessageType.CLIENT_STATUS_MESSAGE):
                break;
            case(MessageType.CONNECTION_ACKNOWLEDGED):

                break;
            default:
                System.err.println("Received message with unkown message type");
                break;
        }

        // Demo Message
        System.out.println("Message from Client: " + msg);
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
            System.err.println("Failed to send message " + msg + ": " +  e);
        }
    }
}
