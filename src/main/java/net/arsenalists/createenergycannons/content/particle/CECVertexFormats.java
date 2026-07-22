package net.arsenalists.createenergycannons.content.particle;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class CECVertexFormats {
    //? if <1.21 {
    public static final VertexFormat PARTICLE_WITH_OVERLAY = new VertexFormat(
        ImmutableMap.<String, VertexFormatElement>builder()
            .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
            .put("UV0", DefaultVertexFormat.ELEMENT_UV0)
            .put("UV1", DefaultVertexFormat.ELEMENT_UV1)
            .put("Color", DefaultVertexFormat.ELEMENT_COLOR)
            .put("UV2", DefaultVertexFormat.ELEMENT_UV2)
            .build()
    );
    //?} else {
    /*public static final VertexFormat PARTICLE_WITH_OVERLAY = VertexFormat.builder()
        .add("Position", VertexFormatElement.POSITION)
        .add("UV0", VertexFormatElement.UV0)
        .add("UV1", VertexFormatElement.UV1)
        .add("Color", VertexFormatElement.COLOR)
        .add("UV2", VertexFormatElement.UV2)
        .build();
    *///?}
}
