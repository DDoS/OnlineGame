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
package ecse414.fall2015.group21.game.shared.connection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Represents an Address, contains an IP number, port number, and shared secret.
 * Used by the networking.
 *
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

    /**
     * Determines if this Address represents a server.
     *
     * @return True is this address represents a server, False otherwise
     */
    public boolean isServer() {
        return type == Type.LOCAL_SERVER || type == Type.REMOTE_SERVER;
    }

    /**
     * Determines if this Address represents a connected client.
     *
     * @return True if this Address represents a connected client, False otherwise
     */
    public boolean isConnectedClient() {
        return type == Type.LOCAL_CLIENT || type == Type.REMOTE_CLIENT;
    }

    /**
     * Determines if thi Address represents an unconnected client.
     *
     * @return True if this Address represents an unconnected client, False otherwise
     */
    public boolean isUnconnectedClient() {
        return type == Type.UNCONNECTED_LOCAL_CLIENT || type == Type.UNCONNECTED_REMOTE_CLIENT;
    }

    /**
     * Determines whether this Address represents a client, either connected or unconnected.
     *
     * @return True if this Address respresents a client, False otherwise
     */
    public boolean isClient() {
        return isConnectedClient() || isUnconnectedClient();
    }

    /**
     * Determines whether this Address is local.
     *
     * @return True if the Address is local, False otherwise
     */
    public boolean isLocal() {
        return type == Type.LOCAL_SERVER || type == Type.UNCONNECTED_LOCAL_CLIENT || type == Type.LOCAL_CLIENT;
    }

    /**
     * Determines whether this Address is remote.
     *
     * @return True if this Address is remote, False otherwise
     */
    public boolean isRemote() {
        return type == Type.REMOTE_SERVER || type == Type.UNCONNECTED_REMOTE_CLIENT || type == Type.REMOTE_CLIENT;
    }

    /**
     * Determines whether this Address has an IP address associated with it. This will be the case when it is not a local Address.
     *
     * @return True if the Address has an IP address associated with it, False otherwise
     */
    public boolean hasIPAddress() {
        return !isLocal();
    }

    /**
     * Determine whether this Address has the shared secret from the server, used for synchronization between client and server.
     *
     * @return True if the Address has the shared secret
     */
    public boolean hasSharedSecret() {
        return isServer() || isConnectedClient();
    }

    /**
     * Get the IP address associated with this Address.
     *
     * @return the IP address associed with the Address
     */
    public int getIPAddress() {
        if (!hasIPAddress()) {
            throw new IllegalStateException("Address doesn't have an IP address");
        }
        return ip;
    }

    /**
     * Get the port number associated with this Address.
     *
     * @return the port number associed with the Address
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the shared secret of this Address if it has one.
     *
     * @return  The shared secret number
     */
    public int getSharedSecret() {
        if (!hasSharedSecret()) {
            throw new IllegalStateException("Address doesn't have a shared secret");
        }
        return sharedSecret;
    }

    /**
     * Gets the Type of this Address. Types include 'REMOTE_SERVER', 'UNCONNECTED_REMOTE_CLIENT', and 'REMOTE_CLIENT'.
     *
      * @return the Type of this Address
     */
    public Type getType() {
        return type;
    }

    /**
     * Connect a client to an Address without specifying a shared secret.
     *
     * @return  the Adress of the connected client
     */
    public Address connectClient() {
        return connectClient(0);
    }

    /**
     * Connect a client to an Address with a given shared secret.
     *
     * @param sharedSecret  the shared secret that will be assocciated with the Address
     * @return              the Address with a given shared secret
     */
    public Address connectClient(int sharedSecret) {
        if (!isUnconnectedClient()) {
            throw new IllegalStateException("Address is not an unconnected client");
        }
        return isLocal() ? forLocalClient(port, sharedSecret) : forRemoteClient(ip, port, sharedSecret);
    }

    /**
     * Get the InetAddress associated with this Address.
     *
     * @return  the InetAddress associated with this Address
     */
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

    /**
     * Get the InetSocketAddress associated with this Address. This is associated with a port number.
     *
     * @return  the InetSocketAddress associated with this Address
     */
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

    /**
     * Enumerator for the different Types of Address.
     */
    public enum Type {
        LOCAL_SERVER,
        REMOTE_SERVER,
        UNCONNECTED_LOCAL_CLIENT,
        UNCONNECTED_REMOTE_CLIENT,
        LOCAL_CLIENT,
        REMOTE_CLIENT
    }

    /**
     * Creates a new Address for a local server.
     *
     * @param port  the port that the local server is listening at
     * @return      the Address associated with the local server
     */
    public static Address forLocalServer(int port) {
        return new Address(0, port, 0, Type.LOCAL_SERVER);
    }

    /**
     * Creates a new Address for a remote server given it's IP address and a port number.
     *
     * @param ipAddress the IP address of the remote server
     * @param port      the port number of the remote server
     * @return          the Address for the remote server
     */
    public static Address forRemoteServer(int ipAddress, int port) {
        return new Address(ipAddress, port, 0, Type.REMOTE_SERVER);
    }

    /**
     * Creates a new Address for an unconnected local client.
     *
     * @param port  port number the local unconnected client will bind to
     * @return      the Address for the unconnected client
     */
    public static Address forUnconnectedLocalClient(int port) {
        return new Address(0, port, 0, Type.UNCONNECTED_LOCAL_CLIENT);
    }

    /**
     * Creates a new Address for an unconnected remote client.
     *
     * @param ipAddress IP address of the remote client
     * @param port      port number of the remote client
     * @return          the Address of the remote unconnectet client
     */
    public static Address forUnconnectedRemoteClient(int ipAddress, int port) {
        return new Address(ipAddress, port, 0, Type.UNCONNECTED_REMOTE_CLIENT);
    }

    /**
     * Creates a new Address for a local (connected) client.
     *
     * @param port          the port number for the connected client
     * @param sharedSecret  the shared secret for the connected client
     * @return              the Address of the connected local client
     */
    public static Address forLocalClient(int port, int sharedSecret) {
        return new Address(0, port, sharedSecret, Type.LOCAL_CLIENT);
    }

    /**
     * Creates a new Address for a remote (connected) client.
     *
     * @param ipAddress     the IP address of the remote client
     * @param port          the port number of the remote client
     * @param sharedSecret  the shared secret for the remote client
     * @return              the Address of the remote connected client
     */
    public static Address forRemoteClient(int ipAddress, int port, int sharedSecret) {
        return new Address(ipAddress, port, sharedSecret, Type.REMOTE_CLIENT);
    }

    /**
     * Creates a new Address for a local unconnected client using the default port number.
     *
     * @return  the Address of the local unconnected client
     */
    public static Address defaultUnconnectedLocalClient() {
        return forUnconnectedLocalClient(DEFAULT_CLIENT_PORT);
    }

    /**
     * Packs 4 bytes representing an IP address into a single integer.
     *
     * @param bytes the bytes representing an IP address that we wish to determine
     * @return      the IP address that the byte string represented
     */
    public static int ipAddressFromBytes(byte... bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Expected 4 bytes in IP address");
        }
        return bytes[0] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
    }

    /**
     * Get the byte string representation for a given IP address in the form xxx.xxx.xxx.xxx
     *
     * @param ipAddress an integer representing an IP address
     * @return          the IP address in the form xxx.xxx.xxx.xxx
     */
    public static String ipToByteString(int ipAddress) {
        return String.valueOf(ipAddress & 0xFF) + '.' + (ipAddress >> 8 & 0xFF) + '.' + (ipAddress >> 16 & 0xFF) + '.' + (ipAddress >> 24 & 0xFF);
    }
}
