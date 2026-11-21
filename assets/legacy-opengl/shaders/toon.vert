// LomiKing toon vertex shader (OpenGL ES 2.0)
uniform mat4 uMVP;
uniform mat4 uModel;
uniform float uOutline;      // 0.0 for the normal pass, ~0.02 for the outline (inverted hull) pass
attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aUV;
varying vec3 vNormal;
varying vec2 vUV;
void main() {
    vec3 p = aPos + aNormal * uOutline;          // push the hull outward for the black outline
    vNormal = mat3(uModel) * aNormal;
    vUV = aUV;
    gl_Position = uMVP * vec4(p, 1.0);
}
