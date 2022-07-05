#include frex:shaders/api/fragment.glsl
#include frex:shaders/lib/math.glsl

// Value near 254
bool isEmissive(float alpha) {
	return 0.9960 < alpha && alpha < 0.9962;
}

void frx_startFragment(inout frx_FragmentData fragData) {
	if (isEmissive(fragData.spriteColor.a)) {
		fragData.emissivity = 1.0;
		fragData.spriteColor.a = 1.0;
	}
}
