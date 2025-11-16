#version 150

// ============= INPUTS =============
// Each vertex of your quad strip should contain:
// - pos   = local space position of the base line (t along bolt)
// - width = half-width of the bolt

in vec3 inPosition;      // x = t (0..1 along the lightning), y = unused, z = offset side (-1 or +1)
in float inWidth;        // per-vertex intended width of bolt


// Sword / model transform
uniform mat4 ModelMatrix;
uniform mat4 ViewMatrix;
uniform mat4 ProjectionMatrix;

// Lightning FX parameters
uniform float GameTime;
uniform float speed;         // controls wobble & travel
uniform float strength;      // thickness multiplier
uniform float animeMix;      // 0=realistic, 1=anime
uniform float splitAmount;   // 0=no fork, 1=full fork

uniform int branchId;        // branch index (0 = main branch)
uniform vec2 branchOffset;   // direction fork goes in model space
uniform float branchSeed;    // each branch should have its own seed

// ============= OUTPUT for FRAGMENT =============
out float vT;               // 0..1 along length
out float vDist;            // distance from bolt centerline
out float vBranch;          // 0,1,2,.. for shading differences
out vec3 vWorld;            // world pos (used for color mapping, optional)


float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float perlin(vec2 p)
{
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash(i);
    float b = hash(i + vec2(1,0));
    float c = hash(i + vec2(0,1));
    float d = hash(i + vec2(1,1));

    vec2 u = f*f*(3.0 - 2.0*f); // smoothing

    return mix(
        mix(a, b, u.x),
        mix(c, d, u.x),
        u.y
    );
}

float perlinFBM(vec2 p)
{
    float f = 0.0;
    float a = 0.7;
    for (int i=0; i<5; i++)
    {
        f += perlin(p) * a;
        p *= 2.0;
        a *= 0.5;
    }
    return f;
}

// REAL
void main()
{
    float t = inPosition.x;        // along-slash param
    float side = inPosition.z;     // -1 or +1 for quad strip

    float time = GameTime * speed;

    // =============  Base curve =============
    // Start with simple straight line along sword swing direction.
    // Instead of hardcoding, use the vertex local X axis as "t direction".

    vec3 basePos = vec3(
    t,                        // along bolt
    0.0,
    0.0
    );

    // ============= Add smooth lightning wiggle =============
    // Perlin-driven signed offset perpendicular to direction
    float wiggle = perlinFBM(vec2(t * 10.0 + branchSeed * 30.0, time * 0.7))
    + perlinFBM(vec2(t * 6.5 + 20.0 + branchSeed, time * 0.4));

    // anime vs realistic: anime = more exaggerated
    float wiggleAmp = mix(0.05, 0.25, animeMix);  // 0.05 realistic → 0.25 anime
    basePos.y += wiggle * wiggleAmp * strength;


    // ============= Branch splitting (true fork geometry) =============
    if (branchId > 0)
    {
        // offset branch away from main trunk
        float forkAngle = mix(0.0, 1.0, splitAmount);

        // smooth falloff: branches split more near middle than at ends
        float forkFactor = smoothstep(0.15, 0.8, t);

        basePos.xy += branchOffset * forkFactor * forkAngle * 0.5;
    }


    // =============  Quad expansion (billboarding) =============
    // The vertices have "side" encoded as +1 or -1.
    // Expand perpendicular to main bolt direction (y-axis here).
    float width = inWidth * strength;

    vec3 expanded = basePos + vec3(
    0.0,            // x dir is forward
    side * width,   // y dir is thickness
    0.0
    );

    // =============  Transform to world -> view -> proj =============
    vec4 world = ModelMatrix * vec4(expanded, 1.0);
    vec4 view  = ViewMatrix  * world;
    vec4 proj  = ProjectionMatrix * view;

    gl_Position = proj;

    // ============= OUTPUT =============
    vWorld = world.xyz;
    vT = t;
    vDist = side;          // signed distance from center
    vBranch = float(branchId);
}
