#version 330

in  vec3 exColour;
in vec3 mvVertexPos;
in vec3 mvVertexNormal;
out vec4 fragColor;

uniform vec3 lightPos;

void main()
{
    vec3 to_light_source  = normalize(lightPos - mvVertexPos);
    float diffusion = max(dot(mvVertexNormal, to_light_source), 0.0);

	fragColor = diffusion * vec4(exColour, 1.0);
}
