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
package ecse414.fall2015.group21.game.server.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Represents a console, which is a command line input and output.
 */
public class Console {
    private final Map<String, Consumer<String[]>> executors = new HashMap<>();
    private final InputStream input;
    private final OutputStream output;
    private final Runnable stopper;
    private volatile Scanner scanner;
    private volatile PrintStream printer;
    private volatile boolean running = false;

    public Console(InputStream input, OutputStream output, Runnable stopper) {
        this.input = input;
        this.output = output;
        this.stopper = stopper;
        executors.put("stop", this::executorStop);
    }

    public void start() {
        scanner = new Scanner(input);
        printer = output instanceof PrintStream ? (PrintStream) output : new PrintStream(output);
        running = true;
        new Thread(this::run).start();
    }

    private void run() {
        while (running && scanner.hasNextLine()) {
            final String[] command = scanner.nextLine().split(" ");
            if (command[0].isEmpty()) {
                continue;
            }
            final Consumer<String[]> executor = executors.get(command[0]);
            if (executor == null) {
                printer.println("Unknown command");
            } else {
                executor.accept(command);
            }
        }
        scanner.close();
        scanner = null;
        printer.close();
        printer = null;
    }

    public void stop() {
        running = false;
        try {
            input.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void executorStop(String[] args) {
        printer.println("Stopping...");
        stopper.run();
    }
}
