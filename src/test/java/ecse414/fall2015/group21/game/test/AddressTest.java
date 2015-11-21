package ecse414.fall2015.group21.game.test;

import java.net.InetSocketAddress;

import ecse414.fall2015.group21.game.shared.connection.Address;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class AddressTest {
    @Test
    public void testAsInetAddress() {
        final Address address = Address.forRemoteServer(0x21436587, (short) 0x1234);
        Assert.assertEquals(0x21436587, address.getIPAddress());
        Assert.assertEquals(0x1234, address.getPort());
        final InetSocketAddress inetSocketAddress = address.asInetSocketAddress();
        Assert.assertArrayEquals(new byte[]{0x21, 0x43, 0x65, (byte) 0x87}, inetSocketAddress.getAddress().getAddress());
        Assert.assertEquals(0x1234, inetSocketAddress.getPort());
    }
}
