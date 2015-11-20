package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetAddress;

/**
 * Represents an address used by the networking.
 */
public class Address {
    private final int ipAddress;
    private final short port;
    private final int sharedSecret;
    private final Type type;

    private Address(int ipAddress, short port, int sharedSecret, Type type) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.sharedSecret = sharedSecret;
        this.type = type;
    }

    public boolean hasIPAddress() {
        return type != Type.LOCAL_SERVER && type != Type.UNCONNECTED_LOCAL_CLIENT && type != Type.LOCAL_CLIENT;
    }

    public int getIPAddress() {
        if (!hasIPAddress()) {
            throw new IllegalStateException("Address doesn't have an IP address");
        }
        return ipAddress;
    }

    public short getPort() {
        return port;
    }

    public boolean hasSharedSecret() {
        return type == Type.LOCAL_CLIENT || type == Type.REMOTE_CLIENT;
    }

    public int getSharedSecret() {
        if (!hasSharedSecret()) {
            throw new IllegalStateException("Address doesn't have a shared secret");
        }
        return sharedSecret;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        LOCAL_SERVER,
        REMOTE_SERVER,
        UNCONNECTED_LOCAL_CLIENT,
        UNCONNECTED_REMOTE_CLIENT,
        LOCAL_CLIENT,
        REMOTE_CLIENT
    }

    public static Address forLocalServer(short port) {
        return new Address(0, port, 0, Type.LOCAL_SERVER);
    }

    public static Address forRemoveServer(int ipAddress, short port) {
        return new Address(ipAddress, port, 0, Type.REMOTE_SERVER);
    }

    public static Address forUnconnectedLocalClient(short port) {
        return new Address(0, port, 0, Type.UNCONNECTED_LOCAL_CLIENT);
    }

    public static Address forUnconnectedRemoteClient(int ipAddress, short port) {
        return new Address(ipAddress, port, 0, Type.UNCONNECTED_REMOTE_CLIENT);
    }

    public static Address forLocalClient(short port, int sharedSecret) {
        return new Address(0, port, sharedSecret, Type.LOCAL_CLIENT);
    }

    public static Address forRemoteClient(int ipAddress, short port, int sharedSecret) {
        return new Address(ipAddress, port, sharedSecret, Type.REMOTE_CLIENT);
    }

    public static int addressToInt(InetAddress address) {
        final byte[] bytes = address.getAddress();
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Expected 4 bytes in IP address");
        }
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | bytes[3] & 0xFF;
    }
}
