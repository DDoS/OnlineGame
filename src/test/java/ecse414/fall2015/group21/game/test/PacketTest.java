package ecse414.fall2015.group21.game.test;

import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeSyncFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.TimeSyncRequestPacket;
import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PacketTest {
    @Test
    public void testConnectRequestUDPPacket() {
        final ConnectRequestPacket.UDP send = new ConnectRequestPacket.UDP(0xDEADBEEF, (short) 0xBABE, 0xBABECAFE);
        final ByteBuf buf = send.asRaw();
        final ConnectRequestPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.ipAddress, receive.ipAddress);
        Assert.assertEquals(send.port, receive.port);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
    }

    @Test
    public void testConnectRequestTCPPacket() {
        final ConnectRequestPacket.TCP send = new ConnectRequestPacket.TCP();
        final ByteBuf buf = send.asRaw();
        final ConnectRequestPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
    }

    @Test
    public void testConnectFulfillUDPPacket() {
        final ConnectFulfillPacket.UDP send = new ConnectFulfillPacket.UDP((short) 0xDAD, 0xDEADCAFEBEEFl);
        final ByteBuf buf = send.asRaw();
        final ConnectFulfillPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.seed, receive.seed);
    }

    @Test
    public void testConnectFulfillTCPPacket() {
        final ConnectFulfillPacket.TCP send = new ConnectFulfillPacket.TCP((short) 0xDAD, 0xDEADCAFEBEEFl);
        final ByteBuf buf = send.asRaw();
        final ConnectFulfillPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.seed, receive.seed);
    }

    @Test
    public void testTimeSyncRequestUDPPacket() {
        final TimeSyncRequestPacket.UDP send = new TimeSyncRequestPacket.UDP(0xDEADBEEF, 0xBABECAFE);
        final ByteBuf buf = send.asRaw();
        final TimeSyncRequestPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
    }

    @Test
    public void testTimeSyncRequestTCPPacket() {
        final TimeSyncRequestPacket.TCP send = new TimeSyncRequestPacket.TCP();
        final ByteBuf buf = send.asRaw();
        final TimeSyncRequestPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
    }

    @Test
    public void testTimeSyncFulfillUDPPacket() {
        final TimeSyncFulfillPacket.UDP send = new TimeSyncFulfillPacket.UDP(0xDEADCAFEBEEFl, 0xDEADBEEF);
        final ByteBuf buf = send.asRaw();
        final TimeSyncFulfillPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
    }

    @Test
    public void testTimeSyncFulfillTCPPacket() {
        final TimeSyncFulfillPacket.TCP send = new TimeSyncFulfillPacket.TCP(0xDEADCAFEBEEFl);
        final ByteBuf buf = send.asRaw();
        final TimeSyncFulfillPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.time, receive.time);
    }

    @Test
    public void testPlayerStateUDPPacket() {
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_STATE, (short) 0xFABE, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }

    @Test
    public void testPlayerStateTCPPacket() {
        final PlayerPacket.TCP send = new PlayerPacket.TCP(Packet.Type.PLAYER_STATE, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }

    @Test
    public void testPlayerShootUDPPacket() {
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_SHOOT, (short) 0xFABE, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }

    @Test
    public void testPlayerShootCPPacket() {
        final PlayerPacket.TCP send = new PlayerPacket.TCP(Packet.Type.PLAYER_SHOOT, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }

    @Test
    public void testPlayerHealthUDPPacket() {
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_HEALTH, (short) 0xFABE, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.UDP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }

    @Test
    public void testPlayerHealthTCPPacket() {
        final PlayerPacket.TCP send = new PlayerPacket.TCP(Packet.Type.PLAYER_HEALTH, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.TCP receive = Packet.TCP.FACTORY.newInstance(buf);
        Assert.assertEquals(send.type, receive.type);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.x, receive.x, 0);
        Assert.assertEquals(send.y, receive.y, 0);
        Assert.assertEquals(send.c, receive.c, 0);
        Assert.assertEquals(send.s, receive.s, 0);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.health, receive.health);
    }
}
