package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.client.renderer.CursedPortalRenderer;

public class ModRenderTypes extends RenderType {
    private ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTask, Runnable clearTask) {
        super(name, format, mode, bufferSize, useDelegate, needsSorting, setupTask, clearTask);
    }

    private static final RenderType PLAYER_BEAM = RenderType.create(
            "player_beam",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            256,
            false,
            true,
            CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .createCompositeState(false)
    );

    public static RenderType getPlayerBeam() {
        return PLAYER_BEAM;
    }
}
