/*
 * This file is part of Online Game, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2015 Group 21
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ecse414.fall2015.group21.game.test;

import ecse414.fall2015.group21.game.shared.data.ConnectFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.ConnectRequestPacket;
import ecse414.fall2015.group21.game.shared.data.Packet;
import ecse414.fall2015.group21.game.shared.data.PlayerPacket;
import ecse414.fall2015.group21.game.shared.data.TimeFulfillPacket;
import ecse414.fall2015.group21.game.shared.data.TimeRequestPacket;
import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class PacketTest {
    @Test
    public void testConnectRequestUDPPacket() {
        final ConnectRequestPacket.UDP send = new ConnectRequestPacket.UDP();
        final ByteBuf buf = send.asRaw();
        final ConnectRequestPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
    }

    @Test
    public void testConnectRequestTCPPacket() {
        final ConnectRequestPacket.TCP send = new ConnectRequestPacket.TCP();
        final ByteBuf buf = send.asRaw();
        final ConnectRequestPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
    }

    @Test
    public void testConnectFulfillUDPPacket() {
        final ConnectFulfillPacket.UDP send = new ConnectFulfillPacket.UDP((short) 0xDAD, 0xDEADCAFEBEEFl, 3848924892l, 0xBABECAFE);
        final ByteBuf buf = send.asRaw();
        final ConnectFulfillPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.seed, receive.seed);
        Assert.assertEquals(send.time, receive.time);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
    }

    @Test
    public void testConnectFulfillTCPPacket() {
        final ConnectFulfillPacket.TCP send = new ConnectFulfillPacket.TCP((short) 0xDAD, 0xDEADCAFEBEEFl, 3848924892l);
        final ByteBuf buf = send.asRaw();
        final ConnectFulfillPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.playerNumber, receive.playerNumber);
        Assert.assertEquals(send.seed, receive.seed);
        Assert.assertEquals(send.time, receive.time);
    }

    @Test
    public void testTimeSyncRequestUDPPacket() {
        final TimeRequestPacket.UDP send = new TimeRequestPacket.UDP(0xDEADBEEF, 0xBABECAFE);
        final ByteBuf buf = send.asRaw();
        final TimeRequestPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
        Assert.assertEquals(send.sharedSecret, receive.sharedSecret);
    }

    @Test
    public void testTimeSyncRequestTCPPacket() {
        final TimeRequestPacket.TCP send = new TimeRequestPacket.TCP(0xDEADBEEF);
        final ByteBuf buf = send.asRaw();
        final TimeRequestPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
    }

    @Test
    public void testTimeSyncFulfillUDPPacket() {
        final TimeFulfillPacket.UDP send = new TimeFulfillPacket.UDP(0xDEADBEEF, 0xDEADCAFEBEEFl);
        final ByteBuf buf = send.asRaw();
        final TimeFulfillPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
        Assert.assertEquals(send.time, receive.time);
    }

    @Test
    public void testTimeSyncFulfillTCPPacket() {
        final TimeFulfillPacket.TCP send = new TimeFulfillPacket.TCP(0xDEADBEEF, 0xDEADCAFEBEEFl);
        final ByteBuf buf = send.asRaw();
        final TimeFulfillPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
        Assert.assertEquals(send.requestNumber, receive.requestNumber);
        Assert.assertEquals(send.time, receive.time);
    }

    @Test
    public void testPlayerStateUDPPacket() {
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_STATE, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD, 0xFABE);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
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
        final PlayerPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
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
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_SHOOT, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD, 0xFABE);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
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
        final PlayerPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
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
        final PlayerPacket.UDP send = new PlayerPacket.UDP(Packet.Type.PLAYER_HEALTH, 0xDEADCAFEBEEFl, 11.321f, 0.042f, 2.34343f, 9.49f, (short) 0xDAD, (short) 0xBAD, 0xFABE);
        final ByteBuf buf = send.asRaw();
        final PlayerPacket.UDP receive = Packet.Type.UDP_FACTORY.newInstance(buf);
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
        final PlayerPacket.TCP receive = Packet.Type.TCP_FACTORY.newInstance(buf);
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
