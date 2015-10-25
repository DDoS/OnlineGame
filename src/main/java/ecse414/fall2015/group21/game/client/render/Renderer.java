package ecse414.fall2015.group21.game.client.render;

import java.util.Collections;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.input.MouseState;
import ecse414.fall2015.group21.game.client.universe.Player;
import ecse414.fall2015.group21.game.client.universe.Universe;
import ecse414.fall2015.group21.game.util.TickingElement;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

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
    private VertexArray shipVertexArray;
    private final TIntObjectMap<Model> shipModels = new TIntObjectHashMap<>();
    private Model cursorModel;

    public Renderer(Client game) {
        super("Renderer", 60);
        this.game = game;
    }

    @Override
    public void onStart() {
        context.setWindowTitle("Game client");
        context.setWindowSize(WIDTH, HEIGHT);
        context.setMSAA(8);
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

        shipVertexArray = context.newVertexArray();
        shipVertexArray.create();
        final TFloatList position = new TFloatArrayList();
        position.add(new float[]{-0.5f, 0.5f, 0, -0.5f, -0.5f, 0, 1, 0, 0});
        final TIntList indices = new TIntArrayList();
        indices.add(new int[]{0, 1, 2});
        shipVertexArray.setData(MeshGenerator.buildMesh(new Vector4i(3, 0, 0, 0), position, null, null, indices));

        final VertexArray cursorVertexArray = context.newVertexArray();
        cursorVertexArray.create();
        cursorVertexArray.setData(MeshGenerator.generateCrosshairs(0.2f));
        cursorVertexArray.setDrawingMode(DrawingMode.LINES);
        cursorModel = new Model(cursorVertexArray, flatMaterial);

        pipeline = new PipelineBuilder()
                .clearBuffer()
                .useCamera(Camera.createOrthographic(Universe.WIDTH, 0, Universe.HEIGHT, 0, 1, -0.01f))
                .renderModels(shipModels.valueCollection())
                .renderModels(Collections.singletonList(cursorModel))
                .updateDisplay()
                .build();
    }

    @Override
    public void onTick(long dt) {
        updatePlayerModels();
        updateMouseCursor();
        pipeline.run(context);
    }

    private void updatePlayerModels() {
        final Universe universe = game.getUniverse();
        final TIntObjectMap<Player> players = universe.getPlayers();
        // Remove models for player no longer in universe
        final TIntObjectIterator<Model> modelIterator = shipModels.iterator();
        while (modelIterator.hasNext()) {
            modelIterator.advance();
            final int number = modelIterator.key();
            if (!players.containsKey(number)) {
                shipModels.remove(number);
            }
        }
        // Update existing players and add new ones
        final TIntObjectIterator<Player> playerIterator = players.iterator();
        while (playerIterator.hasNext()) {
            playerIterator.advance();
            final int number = playerIterator.key();
            Model ship = shipModels.get(number);
            if (ship == null) {
                // Not in list, create new one and add
                ship = new Model(shipVertexArray, flatMaterial);
                ship.getUniforms().add(new Vector4Uniform("color", CausticUtil.LIGHT_GRAY));
                shipModels.put(number, ship);
            }
            final Player player = playerIterator.value();
            ship.setPosition(player.getPosition());
            ship.setRotation(player.getRotation().toQuaternion());
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
        shipVertexArray.destroy();
        shipVertexArray = null;
        shipModels.clear();
        context.destroy();
    }
}
