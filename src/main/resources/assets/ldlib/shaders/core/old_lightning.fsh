#version 150

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

    // Multiple offset times for variation
    float t1 = time * 0.002;
    float t2 = time * 0.0025;
    float t3 = time * 0.0018;

    float lightning = 0.0;

    // Create 4 varied lightning bolts
    for (int i = 0; i < 4; i++) {
        float seed = float(i) * 73.152;
        float xBase = 0.15 + float(i) * 0.25;

        // Different time offset per bolt for variation
        float boltTime = t1 + seed * 0.1;
        float animY = fract(uv.y + boltTime);

        // Segments with varying sizes
        float segSize = 0.03 + random(vec2(seed)) * 0.02;
        float seg = floor(animY / segSize);
        float segFrac = fract(animY / segSize);

        // Smooth zigzag path
        float x1 = (random(vec2(seg, seed)) - 0.5) * 0.2;
        float x2 = (random(vec2(seg + 1.0, seed)) - 0.5) * 0.2;
        float x3 = (random(vec2(seg + 2.0, seed)) - 0.5) * 0.2;

        // Smooth curve through points
        float t = segFrac;
        float smoothX = mix(mix(x1, x2, t), mix(x2, x3, t), t);

        float boltX = xBase + smoothX;
        float dist = abs(uv.x - boltX);

        // Varying thickness along bolt
        float thickness = 0.008 + 0.004 * sin(animY * 30.0 + time * 0.01);

        // Main bolt with streaks
        float bolt = smoothstep(thickness * 3.0, thickness, dist);

        // Add vertical streaks/electricity
        float streak = sin(animY * 80.0 - time * 0.05 + seed * 6.28) * 0.5 + 0.5;
        streak *= sin(animY * 150.0 + time * 0.08 + seed * 3.14) * 0.5 + 0.5;
        bolt *= (0.6 + streak * 0.4);

        // Glow
        float glow = smoothstep(0.08, 0.0, dist) * 0.25;

        // Branches
        float branchChance = random(vec2(seg, seed + 50.0));
        if (branchChance > 0.75) {
            float branchDir = (random(vec2(seg, seed + 100.0)) - 0.5) * 2.0;
            float branchLen = 0.06 + random(vec2(seg, seed + 150.0)) * 0.04;
            float branchX = boltX + branchDir * branchLen * segFrac;
            float branchDist = abs(uv.x - branchX);
            float branch = smoothstep(thickness * 2.0, thickness * 0.5, branchDist) * (1.0 - segFrac * 0.7);
            bolt = max(bolt, branch * 0.6);
        }

        // Flicker per bolt
        float flicker = 0.7 + 0.3 * sin(time * 0.04 + seed);

        // Fade edges
        float fade = smoothstep(0.0, 0.08, animY) * smoothstep(1.0, 0.92, animY);

        lightning += (bolt + glow) * flicker * fade;
    }

    // Sample texture and use as additional detail
    vec2 texUV = vec2(uv.x, fract(uv.y + t2));
    vec4 texColor = texture(Sampler0, texUV);

    // Color gradient
    vec3 color1 = vec3(0.5, 0.7, 1.0);  // Blue
    vec3 color2 = vec3(0.9, 0.95, 1.0); // White-blue
    vec3 boltColor = mix(color1, color2, lightning);

    // Add texture detail to color
    boltColor *= (0.85 + texColor.rgb * 0.15);

    // Final output
    vec3 finalColor = boltColor * lightning * intensity;

    // Smooth alpha with soft edges
    float alpha = pow(lightning, 0.8) * intensity;

    fragColor = vec4(finalColor * vertexColor.rgb, alpha * vertexColor.a);
}
