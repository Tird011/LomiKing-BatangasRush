// LomiKing toon fragment shader: 3 light bands + rim light, or flat black for the outline pass.
precision mediump float;
uniform vec3 uColor;          // material colour from the .mtl Kd
uniform vec3 uLightDir;       // normalized, e.g. (-0.45, 0.75, 0.55)
uniform int uIsOutline;       // 1 on the outline pass (draw with glCullFace(GL_FRONT))
uniform int uIsFace;          // 1 for Mang Kaloy's face decal
uniform sampler2D uFaceAtlas; // textures/face_atlas.png (4 x 2 grid of emotes)
uniform vec2 uEmoteCell;      // which emote: (col,row), e.g. (2,0) = "blow"
varying vec3 vNormal;
varying vec2 vUV;
void main() {
    if (uIsOutline == 1) { gl_FragColor = vec4(0.13, 0.09, 0.10, 1.0); return; }
    if (uIsFace == 1) {
        vec2 uv = (vec2(vUV.x, 1.0 - vUV.y) + uEmoteCell) * vec2(0.25, 0.5);
        vec4 t = texture2D(uFaceAtlas, uv);
        if (t.a < 0.5) discard;                 // only eyes/mouth are drawn; skin comes from the head
        gl_FragColor = vec4(t.rgb, 1.0); return;
    }
    vec3 n = normalize(vNormal);
    float d = dot(n, uLightDir);
    float band = d > 0.3 ? 1.0 : (d > -0.15 ? 0.84 : 0.68);
    gl_FragColor = vec4(uColor * band, 1.0);
}
