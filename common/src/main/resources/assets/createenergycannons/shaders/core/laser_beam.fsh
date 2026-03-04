#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler3;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float GameTime;

in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
flat in int layerIndex;
flat in int cannonPower;

out vec4 fragColor;

// noise

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float val = 0.0;
    val += 0.50 * valueNoise(p);
    val += 0.25 * valueNoise(p * 2.03 + vec2(1.7, 9.2));
    val += 0.125 * valueNoise(p * 4.01 + vec2(5.3, 2.8));
    return val / 0.875;
}

// Ridged noise
float ridgedNoise(vec2 p) {
    return 1.0 - abs(valueNoise(p) * 2.0 - 1.0);
}

float ridgedFbm(vec2 p) {
    float val = 0.0;
    val += 0.5 * ridgedNoise(p);
    val += 0.25 * ridgedNoise(p * 2.1 + vec2(3.1, 7.4));
    return val / 0.75;
}

// 3D noise for volumetric sampling (uses 2D slices at different depths)
float noise3D(vec3 p) {
    float z0 = valueNoise(p.xy + p.z * 17.37);
    float z1 = valueNoise(p.xy + (p.z + 1.0) * 17.37);
    return mix(z0, z1, fract(p.z));
}

float fbm3D(vec3 p) {
    float val = 0.0;
    val += 0.50 * noise3D(p);
    val += 0.25 * noise3D(p * 2.03 + vec3(1.7, 9.2, 4.6));
    val += 0.125 * noise3D(p * 4.01 + vec3(5.3, 2.8, 8.1));
    return val / 0.875;
}

// --- Volumetric cylinder helpers ---
// Treats the quad as a window into a cylinder. centerDist is distance from beam axis
float cylinderThickness(float centerDist) {
    float d = clamp(centerDist, 0.0, 0.999);
    return sqrt(1.0 - d * d);  // chord length through unit circle
}

// Sample the volumetric density along a ray through the cylinder
// u = position along beam, centerDist = radial distance from center, depth = ray parameter
float volumeSample(float u, float centerDist, float depth, float time) {
    // 3D position inside the cylinder: (along beam, radial, depth-into-cylinder)
    vec3 pos = vec3(u * 8.0 + time * 0.3, centerDist * 3.0, depth * 3.0);
    return fbm3D(pos);
}

void main() {
    float u = texCoord0.x;
    float v = texCoord0.y;

    float time = GameTime * 1200.0;
    float powerNorm = float(cannonPower) / 16.0;

    float centerDist = abs(v - 0.5) * 2.0;

    // Cylinder thickness: how much "material" a ray passes through at this point
    float thickness = cylinderThickness(centerDist);

    float edgeNoise = fbm(vec2(u * 10.0 + time * 0.15, v * 5.0) * 1.8);
    float distortedDist = centerDist + (edgeNoise - 0.5) * 0.25;
    distortedDist = clamp(distortedDist, 0.0, 1.0);
    float distortedThickness = cylinderThickness(clamp(distortedDist, 0.0, 0.999));

    // Gradient LUT lookup
    int powerRow = clamp(cannonPower - 1, 0, textureSize(Sampler3, 0).y - 1);
    vec4 gradientColor = texelFetch(Sampler3, ivec2(layerIndex, powerRow), 0);

    float animatedAlpha = 1.0;
    float whiteHot = 0.0;

    float edgeSharpness = mix(0.35, 0.15, powerNorm);

    if (layerIndex == 0) {
        // === CORE: dense volumetric center ===
        // Volumetric accumulation: sample noise at 4 depth slices through the cylinder
        float accumulated = 0.0;
        for (int s = 0; s < 4; s++) {
            float depth = (float(s) + 0.5) / 4.0; // 0.125, 0.375, 0.625, 0.875
            float sample = volumeSample(u, centerDist, depth * thickness, time * 0.8);
            accumulated += sample * thickness; // more material at center = brighter
        }
        accumulated /= 4.0;

        // Tight falloff
        float coreFalloff = pow(distortedThickness, 0.8);

        float pulse = 0.93 + 0.07 * sin(time * 3.0 + u * 5.0);
        animatedAlpha = coreFalloff * (0.7 + 0.3 * accumulated) * pulse;
        animatedAlpha *= 0.85 + 0.15 * powerNorm;

        // Center goes white-hot
        whiteHot = pow(thickness, 1.5) * (0.3 + 0.7 * powerNorm);

    } else if (layerIndex == 1) {
        float accumulated = 0.0;
        for (int s = 0; s < 3; s++) {
            float depth = (float(s) + 0.5) / 3.0;
            vec3 samplePos = vec3(u * 6.0 + time * 0.5, centerDist * 2.0, depth * thickness * 3.0);

            float plasma = noise3D(samplePos + vec3(time * 0.3, 0.0, 0.0));
            float ridges = 1.0 - abs(noise3D(samplePos * 1.5 + vec3(time * 0.4, 0.0, 0.0)) * 2.0 - 1.0);
            ridges = pow(ridges, 2.5);

            accumulated += (plasma * 0.6 + ridges * 0.4) * distortedThickness;
        }
        accumulated /= 3.0;

        // Cylindrical falloff with soft edges
        float innerFalloff = pow(distortedThickness, 0.6);

        float scroll = time * 0.8;
        float wave = sin(u * 15.0 + scroll) * 0.5 + 0.5;
        float wave2 = sin(u * 22.0 + scroll * 1.4 + v * 3.0) * 0.5 + 0.5;
        float hotspot = pow(wave * wave2, 2.0);

        animatedAlpha = innerFalloff * mix(0.3, 1.0, accumulated + hotspot * 0.3);
        animatedAlpha = min(animatedAlpha, 1.0);

        whiteHot = (accumulated * 0.4 + hotspot * 0.15) * innerFalloff * powerNorm;

        float fresnel = (1.0 - thickness) * innerFalloff * 0.35;
        whiteHot += fresnel;

    } else if (layerIndex == 2) {
        // Outer bloom and haze ===
        // Softer, fewer samples needed
        float vol = volumeSample(u, centerDist, thickness * 0.5, time * 0.3);

        float outerFalloff = pow(distortedThickness, 0.4); // very soft spread

        float breathe = 0.6 + 0.4 * sin(time * 0.4 + u * 1.5);
        float shimmer = 0.85 + 0.15 * sin(time * 2.0 + u * 6.0 + v * 3.0);

        animatedAlpha = outerFalloff * breathe * shimmer * (0.6 + 0.4 * vol);

        // Soft edge halo from cylinder viewing geometry
        float edgeHalo = (1.0 - thickness) * outerFalloff * 0.25;
        whiteHot = edgeHalo;

    } else if (layerIndex == 3) {
        // Trendril thingies these will stay flat  ===
        float spark1 = pow(fract(u * 6.0 - time * 2.0), 6.0);
        float spark2 = pow(fract(u * 4.0 + time * 1.2), 6.0);
        float spark3 = pow(fract(u * 9.0 - time * 3.5), 12.0);
        float sparks = max(max(spark1, spark2), spark3);

        float flickerSeed = hash(vec2(floor(time * 10.0), floor(u * 5.0)));
        float flicker = step(0.2, flickerSeed);

        float electricNoise = valueNoise(vec2(u * 30.0 + time * 3.0, v * 12.0));
        electricNoise = pow(electricNoise, 1.5);

        float lightning = ridgedNoise(vec2(u * 15.0 + time * 2.0, v * 6.0));
        lightning = pow(lightning, 3.0);

        float stripFalloff = 1.0 - smoothstep(0.0, 0.5, centerDist);
        float baseGlow = 0.2 * stripFalloff;

        animatedAlpha = stripFalloff * flicker * (sparks * 0.5 + electricNoise * 0.2 + lightning * 0.3) + baseGlow;
        animatedAlpha = min(animatedAlpha, 1.0);

        whiteHot = (sparks * 0.5 + lightning * 0.3) * flicker;

    } else if (layerIndex == 4) {
        // Ambient flicker  ===
        float auraFalloff = pow(max(0.0, distortedThickness), 0.35);

        float slowBreathe = 0.5 + 0.5 * sin(time * 0.25 + u * 1.0);
        float medBreathe = 0.8 + 0.2 * sin(time * 1.2 + u * 3.0 + v * 2.0);
        float jitter = 0.85 + 0.15 * sin(time * 8.0 + hash(vec2(u * 3.0, v * 3.0)) * 25.0);

        // Soft volumetric noise
        float vol = volumeSample(u, centerDist, thickness * 0.5, time * 0.15);

        animatedAlpha = auraFalloff * slowBreathe * medBreathe * jitter * (0.7 + 0.3 * vol);
    }

    // Final composition ---
    // White-hot: center of beam desaturates toward pure white light
    vec3 finalColor = mix(gradientColor.rgb, vec3(1.0), clamp(whiteHot, 0.0, 0.85)) * vertexColor.rgb;

    float finalAlpha = vertexColor.a * animatedAlpha;

    vec4 color = vec4(finalColor, finalAlpha) * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
