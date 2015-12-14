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
package ecse414.fall2015.group21.game.util;

/**
 * Represents a thread that runs at a specific TPS until terminated.
 * <p>
 * This is a library file from com.flowpowered.commons.ticking
 */
public class TPSLimitedThread extends Thread {
    private final TickingElement element;
    private final Timer timer;
    private volatile boolean running = false;

    public TPSLimitedThread(String name, TickingElement element, int tps) {
        super(name);
        this.element = element;
        timer = new Timer(tps);
    }

    @Override
    public void run() {
        try {
            running = true;
            element.onStart();
            timer.start();
            long lastTime = getTime() - (long) (1f / timer.getTps() * 1000000000), currentTime;
            while (running) {
                try {
                    element.onTick((currentTime = getTime()) - lastTime);
                    lastTime = currentTime;
                    timer.sync();
                } catch (Exception ex) {
                    System.err.println("Exception in ticking thread, attempting to stop normally");
                    ex.printStackTrace();
                    break;
                }
            }
            element.onStop();
        } catch (Exception ex) {
            System.err.println("Exception in ticking thread");
            ex.printStackTrace();
        }
    }

    public void terminate() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    private static long getTime() {
        return System.nanoTime();
    }
}