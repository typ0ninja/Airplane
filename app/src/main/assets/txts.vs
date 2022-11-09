

uniform mat4 uMVPMatrix;
attribute vec4 vPosition;
attribute vec2 aTexPos;
    
varying vec2 vTexPos;

void main() {
// the matrix must be included as a modifier of gl_Position
  gl_Position = uMVPMatrix * vPosition;
  
  vTexPos = aTexPos;
}
