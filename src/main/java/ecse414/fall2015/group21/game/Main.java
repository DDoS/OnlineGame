package ecse414.fall2015.group21.game;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.server.Server;
import ecse414.fall2015.group21.game.shared.connection.Address;

import com.flowpowered.caustic.lwjgl.LWJGLUtil;

public class Main {
    public static final Arguments ARGUMENTS = new Arguments();

    public static void main(String[] args) {
        new JCommander(ARGUMENTS, args);
        final Game game;
        switch (ARGUMENTS.mode) {
            case "client":
                LWJGLUtil.deployNatives(null);
                game = new Client();
                break;
            case "server":
                game = new Server();
                break;
            default:
                System.err.println("Not a valid mode: " + ARGUMENTS.mode);
                return;
        }
        game.open();
    }

    public final static class Arguments {
        @Parameter(names = "--mode", description = "client or sever")
        public String mode = "";
        @Parameter(names = "--ip", description = "Client mode: IPv4 or IPv6 server address")
        public String ipAddress = "";
        @Parameter(names = "--port", description = "Server mode: bind port; Client mode: server port")
        public Integer port = Address.DEFAULT_SERVER_PORT;
        @Parameter(names = "--headless", description = "Don't start the client renderer")
        public Boolean headless = false;

        public Address address() {
            switch (mode) {
                case "client":
                    try {
                        return Address.forRemoteServer(Address.ipAddressFromBytes(InetAddress.getLocalHost().getAddress()), port);
                    } catch (UnknownHostException exception) {
                        throw new RuntimeException(exception);
                    }
                case "server":
                    return Address.forLocalServer(port);
                default:
                    throw new IllegalArgumentException("Not a valid mode: " + mode);
            }
        }

        private Arguments() {
        }
    }
}
