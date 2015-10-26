package ecse414.fall2015.group21.game.client.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.client.universe.Bullet;
import ecse414.fall2015.group21.game.client.universe.Player;
import ecse414.fall2015.group21.game.client.universe.Universe;
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
import com.flowpowered.math.vector.Vector4i;

/**
 * The renderer, takes care of rendering the game state to the window.
 */
public class Renderer extends TickingElement {
    private static final int RESOLUTION = 80;
    public static final int WIDTH = Universe.WIDTH * RESOLUTION, HEIGHT = Universe.HEIGHT * RESOLUTION;
    private final Client game;
    private final Context context = GLImplementation.get(LWJGLUtil.GL32_IMPL);
    private Pipeline pipeline;
    private Material flatMaterial;
    private VertexArray playerVertexArray;
    private VertexArray bulletVertexArray;
    private final Map<Player, Model> playerModels = new HashMap<>();
    private final Map<Bullet, Model> bulletModels = new HashMap<>();
    private Model cursorModel;

    public Renderer(Client game) {
        super("Renderer", 60);
        this.game = game;
    }

    @Override
    public void onStart() {
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
        final float edgeCoordinate45Deg = (float) TrigMath.HALF_SQRT_OF_TWO * Universe.PLAYER_RADIUS;
        position.add(new float[]{
                -edgeCoordinate45Deg, edgeCoordinate45Deg, 0,
                -edgeCoordinate45Deg, -edgeCoordinate45Deg,
                0, Universe.PLAYER_RADIUS, 0, 0
        });
        final TIntList indices = new TIntArrayList();
        indices.add(new int[]{0, 1, 2});
        playerVertexArray.setData(MeshGenerator.buildMesh(new Vector4i(3, 0, 0, 0), position, null, null, indices));

        bulletVertexArray = context.newVertexArray();
        bulletVertexArray.create();
        bulletVertexArray.setData(MeshGenerator.generatePlane(new Vector2f(Universe.BULLET_RADIUS, Universe.BULLET_RADIUS).mul(2)));

        final VertexArray cursorVertexArray = context.newVertexArray();
        cursorVertexArray.create();
        cursorVertexArray.setData(MeshGenerator.generateCrosshairs(0.2f));
        cursorVertexArray.setDrawingMode(DrawingMode.LINES);
        cursorModel = new Model(cursorVertexArray, flatMaterial);

        pipeline = new PipelineBuilder()
                .clearBuffer()
                .useCamera(Camera.createOrthographic(Universe.WIDTH, 0, Universe.HEIGHT, 0, 1, -0.01f))
                .renderModels(playerModels.values())
                .renderModels(bulletModels.values())
                .renderModels(Collections.singletonList(cursorModel))
                .updateDisplay()
                .build();
    }

    @Override
    public void onTick(long dt) {
        final Universe universe = game.getUniverse();
        updateModels(universe.getPlayers(), playerModels,
                () -> {
                    final Model model = new Model(playerVertexArray, flatMaterial);
                    model.getUniforms().add(new Vector4Uniform("color", CausticUtil.WHITE));
                    return model;
                },
                (player, model) -> {
                    model.setPosition(player.getPosition().toVector3());
                    model.setRotation(player.getRotation().toQuaternion());
                });
        updateModels(universe.getBullets(), bulletModels,
                () -> {
                    final Model model = new Model(bulletVertexArray, flatMaterial);
                    model.getUniforms().add(new Vector4Uniform("color", CausticUtil.RED));
                    return model;
                },
                (bullet, model) -> model.setPosition(bullet.getPosition().toVector3()));
        updateMouseCursor();
        pipeline.run(context);
    }

    private <T> void updateModels(Set<T> originals, Map<T, Model> models, Supplier<Model> constructor, BiConsumer<T, Model> updater) {
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
                model = constructor.get();
                models.put(original, model);
            }
            updater.accept(original, model);
        }
    }

    private void updateMouseCursor() {
        final MouseState mouse = game.getInput().getMouseState();
        cursorModel.setPosition(new Vector3f(mouse.getX() * Universe.WIDTH, mouse.getY() * Universe.WIDTH, 0));
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
}
