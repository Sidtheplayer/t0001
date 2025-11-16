#version 150
#define PI 3.141592653589793

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float intensity;
uniform float speed;
uniform float rotation;
uniform float rotationot;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// ------- Random + Noise --------
float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898, 78.233))) * 43758.5453);
}

// Hash noise
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = rand(i);
    float b = rand(i + vec2(1.0, 0.0));
    float c = rand(i + vec2(0.0, 1.0));
    float d = rand(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a) * u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}

// Fractal noise (smooth lightning motion)
float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;

    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

// Rotate UV
vec2 rotate(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    return vec2(c * centered.x - s * centered.y, s * centered.x + c * centered.y) + 0.5;
}

void main() {
    // Rotate lightning if needed
    vec2 uv = rotate(texCoord0, rotation * (GameTime * rotationot));

    float time = GameTime * 2.0 * speed;

    // Position of main bolt
    float centerX = 0.5;

    // Smooth Perlin-based bending
    float bend = fbm(vec2(uv.y * 3.0 - time, time * 0.5)) * 0.25 - 0.125;

    // Slowly animated overall wobble
    float wobble = sin(uv.y * 18.0 + time * 4.0) * 0.015;

    float boltX = centerX + bend + wobble;

    float dist = abs(uv.x - boltX);

    // ------- Core + Glow -------
    float coreT = 0.004;  // core width
    float core  = smoothstep(coreT * 2.5, coreT, dist);

    float glow1 = smoothstep(0.04, 0.0, dist);
    float glow2 = smoothstep(0.15, 0.0, dist);

    // ------- Smooth Branching -------
    float branches = 0.0;

    // 3 levels of branch generation
    for (int i = 0; i < 3; i++) {
        float seed = float(i) * 13.37;

        // Branch offset in X
        float bx = boltX + (fbm(vec2(uv.y * 4.0 + seed, time + seed)) - 0.5) * (0.25 + float(i) * 0.1);

        // Smooth fade-out along Y
        float fadeY = pow(uv.y, 1.0 + float(i));

        // Branch thickness
        float bd = abs(uv.x - bx);
        float b = smoothstep(0.016, 0.003, bd) * (1.0 - fadeY);

        branches += b * (0.5 - float(i) * 0.15);
    }

    // Final lightning intensity
    float lightning = core * 1.4 + glow1 * 0.55 + glow2 * 0.25 + branches;

    // ------- Energetic jitter -------
    lightning *= 0.85 + 0.15 * sin(time * 10.0 + uv.y * 40.0);

    // ------- Color palette -------
    vec3 coreColor  = vec3(1.0, 1.0, 1.0);
    vec3 glowColor  = vec3(0.55, 0.75, 1.0);

    vec3 color = mix(glowColor, coreColor, core + branches * 0.8);

    // Optional texture influence
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a > 0.01)
    color = mix(color, tex.rgb, 0.25);

    color *= lightning * intensity;

    float alpha = lightning * intensity;
    fragColor = vec4(color * vertexColor.rgb, alpha * vertexColor.a);
}
