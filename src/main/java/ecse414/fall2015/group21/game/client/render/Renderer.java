package ecse414.fall2015.group21.game.client.render;

import ecse414.fall2015.group21.game.Game;
import ecse414.fall2015.group21.game.util.TickingElement;

import com.flowpowered.caustic.api.GLImplementation;
import com.flowpowered.caustic.api.Pipeline;
import com.flowpowered.caustic.api.Pipeline.PipelineBuilder;
import com.flowpowered.caustic.api.gl.Context;
import com.flowpowered.caustic.api.gl.Context.Capability;
import com.flowpowered.caustic.api.util.CausticUtil;
import com.flowpowered.caustic.lwjgl.LWJGLUtil;

/**
 * The renderer, takes care of rendering the game state to the window.
 */
public class Renderer extends TickingElement {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private final Pipeline pipeline;
    private final Context context = GLImplementation.get(LWJGLUtil.GL32_IMPL);

    public Renderer(Game game) {
        super("Renderer", 60);
        pipeline = new PipelineBuilder()
                .clearBuffer()
                .updateDisplay()
                .build();
    }

    @Override
    public void onStart() {
        context.setWindowTitle("Game client");
        context.setWindowSize(WIDTH, HEIGHT);
        context.create();
        context.setClearColor(CausticUtil.BLACK);
        context.enableCapability(Capability.CULL_FACE);
        context.enableCapability(Capability.DEPTH_TEST);
    }

    @Override
    public void onTick(long dt) {
        pipeline.run(context);
    }

    @Override
    public void onStop() {
        context.destroy();
    }
}
