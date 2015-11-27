package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Represents an address used by the networking.
 */
public class Address {
    // TODO: register with IANA :P
    public static final int DEFAULT_SERVER_PORT = 28_392;
    // Zero is interpreted as any free port by netty
    public static final int DEFAULT_CLIENT_PORT = 0;
    private final int ip;
    private final int port;
    private final int sharedSecret;
    private final Type type;
    // A cache for conversion
    private InetAddress inetAddress = null;

    private Address(int ip, int port, int sharedSecret, Type type) {
        this.ip = ip;
        this.port = port;
        this.sharedSecret = sharedSecret;
        this.type = type;
    }

    public boolean isServer() {
        return type == Type.LOCAL_SERVER || type == Type.REMOTE_SERVER;
    }

    public boolean isConnectedClient() {
        return type == Type.LOCAL_CLIENT || type == Type.REMOTE_CLIENT;
    }

    public boolean isUnconnectedClient() {
        return type == Type.UNCONNECTED_LOCAL_CLIENT || type == Type.UNCONNECTED_REMOTE_CLIENT;
    }

    public boolean isClient() {
        return isConnectedClient() || isUnconnectedClient();
    }

    public boolean isLocal() {
        return type == Type.LOCAL_SERVER || type == Type.UNCONNECTED_LOCAL_CLIENT || type == Type.LOCAL_CLIENT;
    }

    public boolean isRemote() {
        return type == Type.REMOTE_SERVER || type == Type.UNCONNECTED_REMOTE_CLIENT || type == Type.REMOTE_CLIENT;
    }

    public boolean hasIPAddress() {
        return !isLocal();
    }

    public boolean hasSharedSecret() {
        return isServer() || isConnectedClient();
    }

    public int getIPAddress() {
        if (!hasIPAddress()) {
            throw new IllegalStateException("Address doesn't have an IP address");
        }
        return ip;
    }

    public int getPort() {
        return port;
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

    public Address connectClient() {
        return connectClient(0);
    }

    public Address connectClient(int sharedSecret) {
        if (!isUnconnectedClient()) {
            throw new IllegalStateException("Address is not an unconnected client");
        }
        return isLocal() ? forLocalClient(port, sharedSecret) : forRemoteClient(ip, port, sharedSecret);
    }

    public InetAddress asInetAddress() {
        if (inetAddress == null) {
            try {
                inetAddress = InetAddress.getByAddress(new byte[]{
                        (byte) (ip & 0xFF),
                        (byte) (ip >> 8 & 0xFF),
                        (byte) (ip >> 16 & 0xFF),
                        (byte) (ip >> 24 & 0xFF),
                });
            } catch (UnknownHostException exception) {
                throw new RuntimeException(exception);
            }
        }
        return inetAddress;
    }

    public InetSocketAddress asInetSocketAddress() {
        return new InetSocketAddress(asInetAddress(), port);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        final Address address = (Address) other;
        return ip == address.ip && port == address.port;
    }

    @Override
    public int hashCode() {
        int result = ip;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        switch (type) {
            case LOCAL_SERVER:
                return "LocalSever(port = " + port + ")";
            case REMOTE_SERVER:
                return "RemoteServer(ip = " + ipToByteString(ip) + ", port = " + port + ")";
            case UNCONNECTED_LOCAL_CLIENT:
                return "UnconnectedLocalClient(port = " + port + ")";
            case UNCONNECTED_REMOTE_CLIENT:
                return "UnconnectedRemoteClient(ip = " + ipToByteString(ip) + ", port = " + port + ")";
            case LOCAL_CLIENT:
                return "LocalClient(port = " + port + ", sharedSecret = " + sharedSecret + ")";
            case REMOTE_CLIENT:
                return "RemoteClient(ip = " + ipToByteString(ip) + ", port = " + port + ", sharedSecret = " + sharedSecret + ")";
            default:
                throw new UnsupportedOperationException(type.toString());
        }
    }

    public enum Type {
        LOCAL_SERVER,
        REMOTE_SERVER,
        UNCONNECTED_LOCAL_CLIENT,
        UNCONNECTED_REMOTE_CLIENT,
        LOCAL_CLIENT,
        REMOTE_CLIENT
    }

    public static Address forLocalServer(int port) {
        return new Address(0, port, 0, Type.LOCAL_SERVER);
    }

    public static Address forRemoteServer(int ipAddress, int port) {
        return new Address(ipAddress, port, 0, Type.REMOTE_SERVER);
    }

    public static Address forUnconnectedLocalClient(int port) {
        return new Address(0, port, 0, Type.UNCONNECTED_LOCAL_CLIENT);
    }

    public static Address forUnconnectedRemoteClient(int ipAddress, int port) {
        return new Address(ipAddress, port, 0, Type.UNCONNECTED_REMOTE_CLIENT);
    }

    public static Address forLocalClient(int port, int sharedSecret) {
        return new Address(0, port, sharedSecret, Type.LOCAL_CLIENT);
    }

    public static Address forRemoteClient(int ipAddress, int port, int sharedSecret) {
        return new Address(ipAddress, port, sharedSecret, Type.REMOTE_CLIENT);
    }

    public static Address defaultUnconnectedLocalClient() {
        return forUnconnectedLocalClient(DEFAULT_CLIENT_PORT);
    }

    public static int ipAddressFromBytes(byte... bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Expected 4 bytes in IP address");
        }
        return bytes[0] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
    }

    public static String ipToByteString(int ipAddress) {
        return String.valueOf(ipAddress & 0xFF) + '.' + (ipAddress >> 8 & 0xFF) + '.' + (ipAddress >> 16 & 0xFF) + '.' + (ipAddress >> 24 & 0xFF);
    }
}
