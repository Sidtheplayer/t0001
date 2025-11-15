#version 150

uniform sampler2D Sampler0;
uniform sampler3D Sampler2;
uniform float GameTime;
uniform float intensity;
uniform float speed;
uniform float rotation;
uniform float rotationOverTime;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;


// Noise functions from the shader
float rand(vec2 n) {
    return fract(sin(cos(dot(n, vec2(12.9898, 12.1414)))) * 83758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fbm(vec2 n) {
    float total = 0.0;
    float amplitude = 1.0;
    for (int i = 0; i < 5; i++) {
        total += noise(n) * amplitude;
        n += n * 1.7;
        amplitude *= 0.47;
    }
    return total;
}

vec2 rotate(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    return vec2(c * centered.x - s * centered.y, s * centered.x + c * centered.y) + 0.5;
}

void main() {
    vec2 uv = rotate(texCoord0, (rotation * (GameTime * rotationOverTime)));

    float time = GameTime * 1000.0 * speed;

    // Color palette - electric/plasma colors
    const vec3 c1 = vec3(0.5, 0.0, 0.1);
    const vec3 c2 = vec3(0.9, 0.1, 0.0);
    const vec3 c3 = vec3(0.2, 0.1, 0.7);
    const vec3 c4 = vec3(1.0, 0.9, 0.1);
    const vec3 c5 = vec3(0.1);
    const vec3 c6 = vec3(0.9);

    float shift = 1.327 + sin(time * 0.002) / 2.4;
    float dist = 3.5 - sin(time * 0.0004) / 1.89;

    // Transform UV coordinates
    vec2 p = uv * dist;
    p += sin(p.yx * 4.0 + vec2(0.2, -0.3) * time * 0.001) * 0.04;
    p += sin(p.yx * 8.0 + vec2(0.6, 0.1) * time * 0.001) * 0.01;

    p.x -= time * 0.001 / 1.1;

    // Multi-layered FBM for complex plasma effect
    float q = fbm(p - time * 0.0003 + 1.0 * sin(time * 0.001 + 0.5) / 2.0);
    float qb = fbm(p - time * 0.0004 + 0.1 * cos(time * 0.001) / 2.0);
    float q2 = fbm(p - time * 0.00044 - 5.0 * cos(time * 0.001) / 2.0) - 6.0;
    float q3 = fbm(p - time * 0.0009 - 10.0 * cos(time * 0.001) / 15.0) - 4.0;
    float q4 = fbm(p - time * 0.0014 - 20.0 * sin(time * 0.001) / 14.0) + 2.0;
    q = (q + qb - 0.4 * q2 - 2.0 * q3 + 0.6 * q4) / 3.8;

    vec2 speed2 = vec2(0.1, 0.9);
    vec2 r = vec2(
        fbm(p + q / 2.0 + time * 0.001 * speed2.x - p.x - p.y),
        fbm(p + q - time * 0.001 * speed2.y)
    );

    // Color mixing
    vec3 c = mix(c1, c2, fbm(p + r)) + mix(c3, c4, r.x) - mix(c5, c6, r.y);
    vec3 color = vec3(1.0 / (pow(c + 1.61, vec3(4.0))) * cos(shift * uv.y));

    // Fire/plasma core
    color = vec3(1.0, 0.2, 0.05) / (pow((r.y + r.y) * max(0.0, p.y) + 0.1, 4.0));

    // Texture integration
    vec4 texColor = texture(Sampler0, uv * 0.6 + vec2(0.5, 0.1));
    color += (texColor.xyz * 0.01 * pow((r.y + r.y) * 0.65, 5.0) + 0.055) *
             mix(vec3(0.9, 0.4, 0.3), vec3(0.7, 0.5, 0.2), uv.y);

    // Tone mapping
    color = color / (1.0 + max(vec3(0.0), color));

    // Apply intensity
    color *= intensity;

    // Alpha based on brightness
    float alpha = (color.r + color.g + color.b) / 3.0;
    alpha = clamp(alpha * intensity, 0.0, 1.0);

    fragColor = vec4(color * vertexColor.rgb, alpha * vertexColor.a);
}
