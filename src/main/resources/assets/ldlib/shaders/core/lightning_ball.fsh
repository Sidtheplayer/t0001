#version 150
#define PI 3.141592653589793

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float intensity;
uniform float speed;
uniform float rotation;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453);
}

vec2 rotate(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    return vec2(c * centered.x - s * centered.y, s * centered.x + c * centered.y) + 0.5;
}

void main() {
    vec2 uv = rotate(texCoord0, rotation);

    float time = GameTime * 1000.0 * speed;

    // Single lightning bolt at center
    float xBase = 0.5;

    // Animate downward
    float animY = fract(uv.y + time * 0.003);

    // Small segments for sharp zigzags
    float segSize = 0.02;
    float seg = floor(animY / segSize);
    float segFrac = fract(animY / segSize);

    // Current and next segment offsets
    float x1 = (random(vec2(seg, time * 0.0001)) - 0.5) * 0.25;
    float x2 = (random(vec2(seg + 1.0, time * 0.0001)) - 0.5) * 0.25;

    // Sharp transition (less smooth for more jagged lightning)
    float smoothX = mix(x1, x2, smoothstep(0.3, 0.7, segFrac));

    float boltX = xBase + smoothX;
    float dist = abs(uv.x - boltX);

    // Very thin core
    float coreThickness = 0.002;
    float core = smoothstep(coreThickness * 2.0, coreThickness, dist);

    // Medium glow
    float glow = smoothstep(0.04, 0.0, dist) * 0.4;

    // Outer glow
    float outerGlow = smoothstep(0.12, 0.0, dist) * 0.15;

    // Add energetic streaks along the bolt
    float energyStreaks = 0.0;
    for (int i = 0; i < 3; i++) {
        float streakSpeed = 100.0 + float(i) * 50.0;
        float streakPhase = float(i) * 2.094;
        float streak = sin(animY * streakSpeed - time * 0.1 + streakPhase) * 0.5 + 0.5;
        energyStreaks += streak * 0.33;
    }

    // Modulate core with energy
    core *= (0.7 + energyStreaks * 0.3);

    // Random branches
    float branchChance = random(vec2(seg, floor(time * 0.01)));
    if (branchChance > 0.85) {
        float branchDir = (random(vec2(seg, floor(time * 0.01) + 1.0)) - 0.5) * 2.0;
        float branchLen = 0.08;
        float branchProgress = smoothstep(0.0, 1.0, segFrac);
        float branchX = boltX + branchDir * branchLen * branchProgress;
        float branchDist = abs(uv.x - branchX);

        float branch = smoothstep(coreThickness * 3.0, coreThickness, branchDist);
        branch *= (1.0 - branchProgress * 0.8);

        core = max(core, branch * 0.7);
        glow = max(glow, smoothstep(0.03, 0.0, branchDist) * 0.3 * (1.0 - branchProgress * 0.5));
    }

    // Dynamic flicker
    float flicker = 0.85 + 0.15 * sin(time * 0.06 + animY * 20.0);
    flicker *= 0.9 + 0.1 * random(vec2(floor(time * 0.02), 0.0));

    // Fade at edges
    float fade = smoothstep(0.0, 0.1, animY) * smoothstep(1.0, 0.9, animY);

    float lightning = (core * 2.0 + glow + outerGlow) * flicker * fade;

    // Sample texture - ensure it's actually used
    vec4 texColor = texture(Sampler0, texCoord0);

    // If texture has content, blend it with lightning
    // Otherwise just use pure lightning colors
    vec3 coreColor = vec3(1.0, 1.0, 1.0);
    vec3 glowColor = vec3(0.6, 0.8, 1.0);
    vec3 boltColor = mix(glowColor, coreColor, core);

    // Mix texture into the lightning effect
    if (texColor.a > 0.01) {
        boltColor = mix(boltColor, texColor.rgb, 0.4);
    }

    vec3 finalColor = boltColor * lightning * intensity;
    float alpha = pow(lightning, 0.7) * intensity;

    fragColor = vec4(finalColor * vertexColor.rgb, alpha * vertexColor.a);
}
