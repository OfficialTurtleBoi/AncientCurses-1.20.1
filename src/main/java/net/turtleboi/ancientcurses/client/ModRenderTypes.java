package net.turtleboi.ancientcurses.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.turtleboi.ancientcurses.AncientCurses;

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

    private static final ResourceLocation CURSED_PORTAL_TEXTURE = new ResourceLocation(AncientCurses.MOD_ID, "textures/entity/cursed_portal.png");
    public static final RenderType CURSED_PORTAL_RENDER_TYPE = RenderType.create(
            "cursed_portal",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(CURSED_PORTAL_TEXTURE, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(true)
    );

    public static RenderType getPlayerBeam() {
        return PLAYER_BEAM;
    }
}
