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

import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.vector.Vector2f;

/**
 * Anything that has a position in 2D space and time.
 */
public abstract class Positioned {
    protected long time;
    protected Vector2f position;
    protected Complexf rotation;

    protected Positioned(long time, Vector2f position, Complexf rotation) {
        this.time = time;
        this.position = position;
        this.rotation = rotation;
    }

    public long getTime() {
        return time;
    }

    void setTime(long time) {
        this.time = time;
    }

    public Vector2f getPosition() {
        return position;
    }

    void setPosition(Vector2f position) {
        this.position = position;
    }

    void setTimePosition(long time, Vector2f position) {
        this.time = time;
        this.position = position;
    }

    public Complexf getRotation() {
        return rotation;
    }

    void setRotation(Complexf rotation) {
        this.rotation = rotation;
    }
}
