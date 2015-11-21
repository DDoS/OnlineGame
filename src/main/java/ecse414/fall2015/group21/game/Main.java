package ecse414.fall2015.group21.game;

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
        public Integer port = -1;

        public Address address() {
            switch (mode) {
                case "client":
                    final String[] bytes = ipAddress.split("\\.");
                    return Address.forRemoteServer(fromBytes(Byte.parseByte(bytes[0]), Byte.parseByte(bytes[1]), Byte.parseByte(bytes[2]), Byte.parseByte(bytes[3])), port.shortValue());
                case "server":
                    return Address.forLocalServer(port.shortValue());
                default:
                    throw new IllegalArgumentException("Not a valid mode: " + mode);
            }
        }

        private Arguments() {
        }

        public static int fromBytes(byte... bytes) {
            if (bytes.length != 4) {
                throw new IllegalArgumentException("Expected 4 bytes in IP address");
            }
            return bytes[0] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 16 | (bytes[3] & 0xFF) << 24;
        }
    }
}
