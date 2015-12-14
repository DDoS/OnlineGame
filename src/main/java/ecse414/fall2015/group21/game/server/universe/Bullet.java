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
package ecse414.fall2015.group21.game.server.universe;

import java.util.concurrent.atomic.AtomicInteger;

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 *
 */
public class Bullet extends Positioned implements Snapshotable<Bullet> {
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private final int internalID;
    private final int number;

    public Bullet(int number, long time) {
        this(number, time, Vector2f.ZERO, Complexf.IDENTITY);
    }

    public Bullet(int number, long time, Vector2f position, Complexf rotation) {
        this(ID_COUNTER.getAndIncrement(), number, time, position, rotation);
    }

    private Bullet(int internalID, int number, long time, Vector2f position, Complexf rotation) {
        super(time, position, rotation);
        this.internalID = internalID;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public Bullet snapshot() {
        return new Bullet(internalID, number, time, position, rotation);
    }

    @Override
    public boolean equals(Object that) {
        return this == that || that instanceof Bullet && internalID == ((Bullet) that).internalID;
    }

    @Override
    public int hashCode() {
        return internalID;
    }
}
