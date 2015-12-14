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
package ecse414.fall2015.group21.game.client.render;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import ecse414.fall2015.group21.game.client.input.Input;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.client.universe.RemoteUniverse;
import ecse414.fall2015.group21.game.server.universe.Bullet;
import ecse414.fall2015.group21.game.server.universe.Player;
import ecse414.fall2015.group21.game.server.universe.Universe;
import ecse414.fall2015.group21.game.util.TickingElement;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import com.flowpowered.caustic.api.Camera;
import com.flowpowered.caustic.api.GLImplementation;
import com.flowpowered.caustic.api.Material;
import com.flowpowered.caustic.api.Pipeline;
import com.flowpowered.caustic.api.Pipeline.PipelineBuilder;
import com.flowpowered.caustic.api.data.ShaderSource;
import com.flowpowered.caustic.api.data.Uniform.Vector4Uniform;
import com.flowpowered.caustic.api.gl.Context;
import com.flowpowered.caustic.api.gl.Context.Capability;
import com.flowpowered.caustic.api.gl.Program;
import com.flowpowered.caustic.api.gl.Shader;
import com.flowpowered.caustic.api.gl.VertexArray;
import com.flowpowered.caustic.api.gl.VertexArray.DrawingMode;
import com.flowpowered.caustic.api.model.Model;
import com.flowpowered.caustic.api.util.CausticUtil;
import com.flowpowered.caustic.api.util.MeshGenerator;
import com.flowpowered.caustic.lwjgl.LWJGLUtil;
import com.flowpowered.math.TrigMath;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import com.flowpowered.math.vector.Vector4i;

/**
 * The renderer, takes care of rendering the game state to the window.
 */
public class Renderer extends TickingElement {
    private static final int RESOLUTION = 80;
    public static final int WIDTH = RemoteUniverse.WIDTH * RESOLUTION, HEIGHT = RemoteUniverse.HEIGHT * RESOLUTION;
    private final Input input;
    private final Universe universe;
    private final Context context = GLImplementation.get(LWJGLUtil.GL32_IMPL);
    private Pipeline pipeline;
    private Material flatMaterial;
    private VertexArray playerVertexArray;
    private VertexArray bulletVertexArray;
    private final Map<Player, Model> playerModels = new HashMap<>();
    private final Map<Bullet, Model> bulletModels = new HashMap<>();
    private Model cursorModel;

    public Renderer(Input input, Universe universe) {
        super("Renderer", 60);
        this.input = input;
        this.universe = universe;
    }

    @Override
    public void onStart() {
        CausticUtil.setDebugEnabled(false);
        context.setWindowTitle("Game client");
        context.setWindowSize(WIDTH, HEIGHT);
        context.create();
        context.setClearColor(CausticUtil.BLACK);
        context.enableCapability(Capability.CULL_FACE);
        context.enableCapability(Capability.DEPTH_TEST);

        final Shader flatVertShader = context.newShader();
        flatVertShader.create();
        flatVertShader.setSource(new ShaderSource(getClass().getResourceAsStream("/shaders/flat.vert")));
        flatVertShader.compile();
        final Shader flatFragShader = context.newShader();
        flatFragShader.create();
        flatFragShader.setSource(new ShaderSource(getClass().getResourceAsStream("/shaders/flat.frag")));
        flatFragShader.compile();

        final Program flatProgram = context.newProgram();
        flatProgram.create();
        flatProgram.attachShader(flatVertShader);
        flatProgram.attachShader(flatFragShader);
        flatProgram.link();

        flatMaterial = new Material(flatProgram);
        flatMaterial.getUniforms().add(new Vector4Uniform("color", CausticUtil.WHITE));

        playerVertexArray = context.newVertexArray();
        playerVertexArray.create();
        final TFloatList position = new TFloatArrayList();
        final float edgeCoordinate45Deg = (float) TrigMath.HALF_SQRT_OF_TWO * RemoteUniverse.PLAYER_RADIUS;
        position.add(new float[]{
                -edgeCoordinate45Deg, edgeCoordinate45Deg, 0,
                -edgeCoordinate45Deg, -edgeCoordinate45Deg, 0,
                RemoteUniverse.PLAYER_RADIUS, 0, 0
        });
        final TIntList indices = new TIntArrayList();
        indices.add(new int[]{0, 1, 2});
        playerVertexArray.setData(MeshGenerator.buildMesh(new Vector4i(3, 0, 0, 0), position, null, null, indices));

        bulletVertexArray = context.newVertexArray();
        bulletVertexArray.create();
        bulletVertexArray.setData(MeshGenerator.generatePlane(new Vector2f(RemoteUniverse.BULLET_RADIUS, RemoteUniverse.BULLET_RADIUS).mul(2)));

        final VertexArray cursorVertexArray = context.newVertexArray();
        cursorVertexArray.create();
        cursorVertexArray.setData(MeshGenerator.generateCrosshairs(0.2f));
        cursorVertexArray.setDrawingMode(DrawingMode.LINES);
        cursorModel = new Model(cursorVertexArray, flatMaterial);

        pipeline = new PipelineBuilder()
                .clearBuffer()
                .useCamera(Camera.createOrthographic(RemoteUniverse.WIDTH, 0, RemoteUniverse.HEIGHT, 0, 1, -0.01f))
                .renderModels(playerModels.values())
                .renderModels(bulletModels.values())
                .renderModels(Collections.singletonList(cursorModel))
                .updateDisplay()
                .build();
    }

    @Override
    public void onTick(long dt) {
        updateModels(universe.getPlayers().values(), playerModels,
                player -> {
                    final Model model = new Model(playerVertexArray, flatMaterial);
                    model.getUniforms().add(new Vector4Uniform("color", generateColor(universe.getSeed(), player.getNumber())));
                    return model;
                },
                (player, model) -> {
                    model.setPosition(player.getPosition().toVector3());
                    model.setRotation(player.getRotation().toQuaternion());
                });
        updateModels(universe.getBullets(), bulletModels,
                bullet -> {
                    final Model model = new Model(bulletVertexArray, flatMaterial);
                    model.getUniforms().add(new Vector4Uniform("color", CausticUtil.RED));
                    return model;
                },
                (bullet, model) -> {
                    model.setPosition(bullet.getPosition().toVector3());
                    model.setRotation(bullet.getRotation().toQuaternion());
                });
        updateMouseCursor();
        pipeline.run(context);
    }

    private <T> void updateModels(Collection<T> originals, Map<T, Model> models, Function<T, Model> constructor, BiConsumer<T, Model> updater) {
        // Remove models for entities no longer in universe
        for (Iterator<T> iterator = models.keySet().iterator(); iterator.hasNext(); ) {
            final T modelKey = iterator.next();
            if (!originals.contains(modelKey)) {
                iterator.remove();
            }
        }
        // Update existing entities and add new ones
        for (T original : originals) {
            Model model = models.get(original);
            if (model == null) {
                // Not in list, create new one and add
                model = constructor.apply(original);
                models.put(original, model);
            }
            updater.accept(original, model);
        }
    }

    private void updateMouseCursor() {
        final MouseState mouse = input.getMouseState();
        cursorModel.setPosition(new Vector3f(mouse.getX() * RemoteUniverse.WIDTH, mouse.getY() * RemoteUniverse.WIDTH, 0));
        mouse.clearAll();
    }

    @Override
    public void onStop() {
        cursorModel.getVertexArray().destroy();
        cursorModel = null;
        flatMaterial.getProgram().getShaders().forEach(Shader::destroy);
        flatMaterial.getProgram().destroy();
        flatMaterial = null;
        playerVertexArray.destroy();
        playerVertexArray = null;
        playerModels.clear();
        context.destroy();
    }

    private static Vector4f generateColor(long seed, int number) {
        final float minS = 0.15f, maxS = 1;
        final float minV = 0.7f, maxV = 1;
        final float h = hashToFloat(1, number, seed);
        final float s = hashToFloat(2, number, seed) * (maxS - minS) + minS;
        final float v = hashToFloat(3, number, seed) * (maxV - minV) + minV;
        return hsvToRGB(h, s, v);
    }

    private static float hashToFloat(int x, int y, long seed) {
        final long hash = x * 73428767 ^ y * 9122569 ^ seed * 457;
        return (hash * (hash + 456149) & 0x00ffffff) / (float) 0x01000000;
    }

    private static Vector4f hsvToRGB(float hue, float saturation, float value) {
        final int h = (int) (hue * 6);
        final float f = hue * 6 - h;
        final float p = value * (1 - saturation);
        final float q = value * (1 - f * saturation);
        final float t = value * (1 - (1 - f) * saturation);
        final float r, g, b;
        switch (h) {
            case 0:
                r = value;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = value;
                b = p;
                break;
            case 2:
                r = p;
                g = value;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = value;
                break;
            case 4:
                r = t;
                g = p;
                b = value;
                break;
            case 5:
                r = value;
                g = p;
                b = q;
                break;
            default:
                throw new RuntimeException();
        }
        return new Vector4f(r, g, b, 1);
    }
}
