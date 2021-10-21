#version 330

in  vec3 exColour;
in vec3 mvVertexPos;
in vec3 mvVertexNormal;
out vec4 fragColor;

uniform vec3 lightPos;

void main()
{
    vec3 light_direction = lightPos - mvVertexPos;
    vec3 to_light_source  = normalize(light_direction);
    float diffuseFactor = max(dot(mvVertexNormal, to_light_source ), 0.0);

	fragColor = diffuseFactor * vec4(exColour, 1.0);
}
