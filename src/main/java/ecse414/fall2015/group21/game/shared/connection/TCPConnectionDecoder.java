package ecse414.fall2015.group21.game.shared.connection;

import java.util.List;

import ecse414.fall2015.group21.game.shared.data.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 *
 */
public class TCPConnectionDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() <= 0) {
            return;
        }
        // Get the packet type from the first byte
        final byte id = in.getByte(in.readerIndex());
        if (id >= Packet.Type.BY_ID.length) {
            throw new IllegalArgumentException("Not a packet type: " + id);
        }
        // Check if all of the packet has been received
        final int length = Packet.Type.BY_ID[id].tcpLength;
        if (in.readableBytes() < length) {
            return;
        }
        // If so split it into a new buffer and add it to the output
        out.add(in.readBytes(length));
    }
}
