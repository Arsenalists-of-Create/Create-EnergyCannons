package net.arsenalists.createenergycannons.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/**
 * Extends RenderType solely to access its protected static shard constants,
 * allowing us to build custom RenderTypes with polygon offset for decal rendering.
 */
public final class CECRenderTypes extends RenderType {

    // Never instantiated; constructor exists only to satisfy the abstract class
    private CECRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                           int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                           Runnable setup, Runnable clear) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
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
