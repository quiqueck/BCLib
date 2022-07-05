#version 150
#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

vec3 rgbToHSV(vec3 color) {
	vec4 k = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
	vec4 p = mix(vec4(color.bg, k.wz), vec4(color.gb, k.xy), step(color.b, color.g));
	vec4 q = mix(vec4(p.xyw, color.r), vec4(color.r, p.yzx), step(p.x, color.r));
	float d = q.x - min(q.w, q.y);
	float e = 1.0e-10;
	return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsvToRGB(vec3 color) {
	vec4 k = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
	vec3 p = abs(fract(color.xxx + k.xyz) * 6.0 - k.www);
	return color.z * mix(k.xxx, clamp(p - k.xxx, 0.0, 1.0), color.y);
}

// Value near 254
bool isEmissive(float alpha) {
	return 0.9960 < alpha && alpha < 0.9962;
}

void main() {
	vec4 tex = texture(Sampler0, texCoord0);
	if (tex.a < 0.1) {
		discard;
	}
	vec4 color = tex * ColorModulator;
	vec4 vertex = vertexColor;
	if (isEmissive(tex.a)) {
		vec3 hsv = rgbToHSV(vertex.rgb);
		hsv.z = 1.0;
		vertex.rgb = hsvToRGB(hsv);
		color.a = 1.0;
	}
	color = linear_fog(color * vertex, vertexDistance, FogStart, FogEnd, FogColor);
	fragColor = color;
}
