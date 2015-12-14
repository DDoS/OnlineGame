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
        public String mode = "server";
        @Parameter(names = "--type", description = "udp or tcp")
        public String type = "udp";
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
                        return Address.forRemoteServer(Address.ipAddressFromBytes(InetAddress.getByName(ipAddress).getAddress()), port);
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
