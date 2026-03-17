#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler3;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
flat in int cannonPower;

out vec4 fragColor;

// Inlined fog to avoid #moj_import <fog.glsl> which Iris/OptiFine patches
vec4 cec_linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (vertexDistance <= fogStart) {
        return inColor;
    }
    float fogValue = vertexDistance < fogEnd ? smoothstep(fogEnd, fogStart, vertexDistance) : 0.0;
    return vec4(mix(fogColor.rgb, inColor.rgb, fogValue) * inColor.a, inColor.a);
}

void main() {
    vec4 sourceColor = texture(Sampler0, texCoord0);
    if (sourceColor.a < 0.1) {
        discard;
    }
    ivec2 texCoord1 = ivec2(floor(sourceColor.r * 255), clamp(cannonPower - 1, 0, textureSize(Sampler3, 0).y - 1));
    vec4 color = vec4(texelFetch(Sampler3, texCoord1, 0).rgb, sourceColor.a) * vertexColor * ColorModulator;
    fragColor = cec_linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
