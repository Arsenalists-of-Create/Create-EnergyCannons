package net.arsenalists.createenergycannons.content.particle;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class CECVertexFormats {
    public static final VertexFormat PARTICLE_WITH_OVERLAY = new VertexFormat(
        ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV1", DefaultVertexFormat.ELEMENT_UV1)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .build()
    );
}
