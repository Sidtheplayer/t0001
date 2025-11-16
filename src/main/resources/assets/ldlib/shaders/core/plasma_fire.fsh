#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;
uniform float GameTime;
uniform float intensity;
uniform float speed;
uniform float rotation;
uniform float rotationOverTime;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;


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

vec2 rotateUV(vec2 uv, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    vec2 centered = uv - 0.5;
    vec2 r = vec2(c * centered.x - s * centered.y, s * centered.x + c * centered.y) + 0.5;
    return r;
}

void main() {

    float rotationAngle = rotation + GameTime * (rotationOverTime * 20);
    vec2 uv = rotateUV(texCoord0, rotationAngle);


    float time = GameTime * speed * pow(10 , 3);


    const vec3 c1 = vec3(0.5, 0.0, 0.1);
    const vec3 c2 = vec3(0.9, 0.1, 0.0);
    const vec3 c3 = vec3(0.2, 0.1, 0.7);
    const vec3 c4 = vec3(1.0, 0.9, 0.1);

    float shift = 1.327 + sin(time * 0.002) * 0.4167; // scaled
    float dist = 3.5 - sin(time * 0.0004) * 0.529;    // scaled

    vec2 p = uv * dist;
    p += sin(p.yx * 4.0 + vec2(0.2, -0.3) * time * 0.001) * 0.04;
    p += sin(p.yx * 8.0 + vec2(0.6, 0.1) * time * 0.001) * 0.01;
    p.x -= time * 0.001 / 1.1;

    float q  = fbm(p - time * 0.0003 + 1.0 * sin(time * 0.001 + 0.5) / 2.0);
    float qb = fbm(p - time * 0.0004 + 0.1 * cos(time * 0.001) / 2.0);
    float q2 = fbm(p - time * 0.00044 - 5.0 * cos(time * 0.001) / 2.0) - 6.0;
    float q3 = fbm(p - time * 0.0009 - 10.0 * cos(time * 0.001) / 15.0) - 4.0;
    float q4 = fbm(p - time * 0.0014 - 20.0 * sin(time * 0.001) / 14.0) + 2.0;
    q = (q + qb - 0.4 * q2 - 2.0 * q3 + 0.6 * q4) / 3.8;

    vec2 speed2 = vec2(0.1, 0.9);
    vec2 r = vec2(
    fbm(p + q * 0.5 + time * 0.001 * speed2.x - p.x - p.y),
    fbm(p + q - time * 0.001 * speed2.y)
    );


    vec3 mix1 = mix(c1, c2, clamp(fbm(p + r), 0.0, 1.0));
    vec3 mix2 = mix(c3, c4, clamp(r.x, 0.0, 1.0));
    vec3 c = mix1 + mix2;


    vec3 safeBase = abs(c) + vec3(1.61);
    vec3 color = (1.0 / pow(safeBase, vec3(4.0))) * cos(shift * uv.y);


    float denom = max(0.0001, (r.y + r.y) * max(0.0, p.y) + 0.1);
    color = vec3(1.0, 0.2, 0.05) / pow(denom, 4.0);


    vec4 texColor = texture(Sampler0, uv * 0.6 + vec2(0.5, 0.1));
    float addTerm = pow(max(0.0, (r.y + r.y) * 0.65), 5.0);
    color += (texColor.xyz * 0.01 * addTerm + 0.055) * mix(vec3(0.9, 0.4, 0.3), vec3(0.7, 0.5, 0.2), uv.y);


    color = color / (1.0 + max(vec3(0.0), color));
    color *= intensity;

    float alpha = clamp((color.r + color.g + color.b) / 3.0 * intensity, 0.0, 1.0);

    fragColor = vec4(color * vertexColor.rgb, alpha * vertexColor.a);
}
