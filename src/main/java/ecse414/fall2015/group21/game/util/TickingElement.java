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
 * Represents an element that ticks at a specific TPS.
 * <p>
 * This is a library file from com.flowpowered.commons.ticking
 */
public abstract class TickingElement {
    private final String name;
    private final int tps;
    private volatile TPSLimitedThread thread;

    public TickingElement(String name, int tps) {
        this.name = name;
        this.tps = tps;
    }

    public final void start() {
        synchronized (this) {
            if (thread == null) {
                thread = new TPSLimitedThread(name, this, tps);
                thread.start();
            }
        }
    }

    public final void stop() {
        synchronized (this) {
            if (thread != null) {
                thread.terminate();
                thread = null;
            }
        }
    }

    public final boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    public TPSLimitedThread getThread() {
        return thread;
    }

    public abstract void onStart();

    public abstract void onTick(long dt);

    public abstract void onStop();
}
