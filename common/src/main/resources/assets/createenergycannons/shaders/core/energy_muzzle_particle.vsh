#version 150

in vec3 Position;
in vec2 UV0;
in ivec2 UV1;
in vec4 Color;
in ivec2 UV2;

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out float vertexDistance;
out vec2 texCoord0;
out vec4 vertexColor;
flat out int cannonPower;

// Inlined fog to avoid #moj_import <fog.glsl> which Iris/OptiFine patches
float cec_fog_distance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos, 1.0)).xz);
        float distY = abs((modelViewMat * vec4(pos, 1.0)).y);
        return max(distXZ, distY);
    }
}

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = cec_fog_distance(ModelViewMat, Position, FogShape);
    texCoord0 = UV0;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
    cannonPower = UV1.y;
}
