package ecse414.fall2015.group21.game.shared.connection;

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
        return isConnectedClient();
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

    public int getSharedSecret() {
        if (!hasSharedSecret()) {
            throw new IllegalStateException("Address doesn't have a shared secret");
        }
        return sharedSecret;
    }

    public Type getType() {
        return type;
    }

    public Address connectClient(int sharedSecret) {
        if (!isUnconnectedClient()) {
            throw new IllegalStateException("Address is not an unconnected client");
        }
        return isLocal() ? forLocalClient(port, sharedSecret) : forRemoteClient(ipAddress, port, sharedSecret);
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

    public static Address forRemoteServer(int ipAddress, short port) {
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
}
