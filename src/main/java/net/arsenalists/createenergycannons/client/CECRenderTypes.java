package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Subclasses RenderType to access protected shader/layering constants
 * for custom decal rendering (polygon offset).
 */
public final class CECRenderTypes extends RenderType {

    // Never instantiated; constructor exists only to satisfy the abstract class
    private CECRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                           int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                           Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
    }



    private static final RenderType LASER_BEAM_FALLBACK = create(
            "cec_laser_beam_fallback",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            4096, false, true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_TEXT_SHADER)
                    .setTextureState(new TextureStateShard(
                            net.arsenalists.createenergycannons.CECMod.resource("textures/beam/beam_noise.png"),
                            false, false))
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .createCompositeState(false)
    );

    public static RenderType laserBeamFallback() {
        return LASER_BEAM_FALLBACK;
    }

    public static RenderType burnDecal(ResourceLocation texture) {
        CompositeState state = CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setCullState(CULL)
                .setLayeringState(POLYGON_OFFSET_LAYERING)
                .createCompositeState(true);
        return create("cec_burn_decal", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, true, true, state);
    }
}
