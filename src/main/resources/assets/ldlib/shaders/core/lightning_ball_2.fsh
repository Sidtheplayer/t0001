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

// ------- Random + Noise --------
float rand(vec2 n) {
    return fract(sin(dot(n, vec2(12.9898, 78.233))) * 43758.5453);
}

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

vec2 rotate(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    return vec2(c * centered.x - s * centered.y, s * centered.x + c * centered.y) + 0.5;
}

void main() {
    // Fixed rotation - just use the rotation uniform directly
    vec2 uv = rotate(texCoord0, rotation);

    float time = GameTime * 1000.0 * speed;

    float centerX = 0.5;

    // Smooth Perlin-based bending
    float bend = fbm(vec2(uv.y * 3.0 - time * 0.001, time * 0.0005)) * 0.25 - 0.125;

    // Slowly animated overall wobble
    float wobble = sin(uv.y * 18.0 + time * 0.004) * 0.015;

    float boltX = centerX + bend + wobble;
    float dist = abs(uv.x - boltX);

    // ------- Core + Glow -------
    float coreT = 0.004;
    float core = smoothstep(coreT * 2.5, coreT, dist);

    float glow1 = smoothstep(0.04, 0.0, dist);
    float glow2 = smoothstep(0.15, 0.0, dist);

    // ------- Smooth Branching -------
    float branches = 0.0;

    for (int i = 0; i < 3; i++) {
        float seed = float(i) * 13.37;

        float bx = boltX + (fbm(vec2(uv.y * 4.0 + seed, time * 0.001 + seed)) - 0.5) * (0.25 + float(i) * 0.1);

        float fadeY = pow(uv.y, 1.0 + float(i));

        float bd = abs(uv.x - bx);
        float b = smoothstep(0.016, 0.003, bd) * (1.0 - fadeY);

        branches += b * (0.5 - float(i) * 0.15);
    }

    float lightning = core * 1.4 + glow1 * 0.55 + glow2 * 0.25 + branches;

    // Energetic jitter
    lightning *= 0.85 + 0.15 * sin(time * 0.01 + uv.y * 40.0);

    // ------- Color palette -------
    vec3 coreColor = vec3(1.0, 1.0, 1.0);
    vec3 glowColor = vec3(0.55, 0.75, 1.0);

    vec3 color = mix(glowColor, coreColor, core + branches * 0.8);

    // Optional texture influence
    vec4 tex = texture(Sampler0, texCoord0);
    if (tex.a > 0.01) {
        color = mix(color, tex.rgb, 0.25);
    }

    color *= lightning * intensity;

    float alpha = clamp(lightning * intensity, 0.0, 1.0);
    fragColor = vec4(color * vertexColor.rgb, alpha * vertexColor.a);
}