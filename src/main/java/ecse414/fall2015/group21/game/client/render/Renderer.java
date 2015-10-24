package ecse414.fall2015.group21.game.client.render;

import ecse414.fall2015.group21.game.client.Client;
import ecse414.fall2015.group21.game.client.world.Player;
import ecse414.fall2015.group21.game.client.world.Physics;
import ecse414.fall2015.group21.game.util.TickingElement;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import com.flowpowered.caustic.api.Camera;
import com.flowpowered.caustic.api.GLImplementation;
import com.flowpowered.caustic.api.Material;
import com.flowpowered.caustic.api.Pipeline;
import com.flowpowered.caustic.api.Pipeline.PipelineBuilder;
import com.flowpowered.caustic.api.data.ShaderSource;
import com.flowpowered.caustic.api.gl.Context;
import com.flowpowered.caustic.api.gl.Context.Capability;
import com.flowpowered.caustic.api.gl.Program;
import com.flowpowered.caustic.api.gl.Shader;
import com.flowpowered.caustic.api.gl.VertexArray;
import com.flowpowered.caustic.api.model.Model;
import com.flowpowered.caustic.api.util.CausticUtil;
import com.flowpowered.caustic.api.util.MeshGenerator;
import com.flowpowered.caustic.lwjgl.LWJGLUtil;
import com.flowpowered.math.vector.Vector2f;

/**
 * The renderer, takes care of rendering the game state to the window.
 */
public class Renderer extends TickingElement {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private final Client game;
    private final Pipeline pipeline;
    private final Context context = GLImplementation.get(LWJGLUtil.GL32_IMPL);
    private Material flatMaterial;
    private VertexArray playerVertexArray;
    private final TIntObjectMap<Model> playerModels = new TIntObjectHashMap<>();

    public Renderer(Client game) {
        super("Renderer", 60);
        this.game = game;
        pipeline = new PipelineBuilder()
                .clearBuffer()
                .useCamera(Camera.createOrthographic(WIDTH, 0, HEIGHT, 0, 0, 1))
                .renderModels(playerModels.valueCollection())
                .updateDisplay()
                .build();
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

        playerVertexArray = context.newVertexArray();
        playerVertexArray.create();
        playerVertexArray.setData(MeshGenerator.generatePlane(Vector2f.ONE.mul(10)));
    }

    @Override
    public void onTick(long dt) {
        updatePlayerModels();
        pipeline.run(context);
    }

    private void updatePlayerModels() {
        final Physics physics = game.getPhysics();
        final TIntObjectMap<Model> newPlayerModels = new TIntObjectHashMap<>();
        for (Player player : physics.getPlayers()) {
            Model model = playerModels.get(player.getNumber());
            if (model == null) {
                model = new Model(playerVertexArray, flatMaterial);
            }
            model.setPosition(player.getPosition());
            newPlayerModels.put(player.getNumber(), model);
        }
        playerModels.clear();
        playerModels.putAll(newPlayerModels);
    }

    @Override
    public void onStop() {
        flatMaterial.getProgram().getShaders().forEach(Shader::destroy);
        flatMaterial.getProgram().destroy();
        flatMaterial = null;
        playerVertexArray.destroy();
        playerVertexArray = null;
        context.destroy();
    }
}
