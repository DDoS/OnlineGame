package ecse414.fall2015.group21.game;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.server.Server;

import com.flowpowered.caustic.lwjgl.LWJGLUtil;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Expected mode argument");
            return;
        }
        final Game game;
        switch (args[0]) {
            case "client":
                LWJGLUtil.deployNatives(null);
                game = new Client();
                break;
            case "server":
                game = new Server();
                break;
            default:
                System.err.println("Not a valid mode");
                return;
        }
        game.open();
    }
}
