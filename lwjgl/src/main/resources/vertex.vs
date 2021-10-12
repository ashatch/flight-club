#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 inColour;
layout (location=2) in vec3 vertexNormal;

out vec3 exColour;
out vec3 mvVertexPos;
out vec3 mvVertexNormal;

uniform mat4 worldMatrix;
uniform mat4 projectionMatrix;

void main()
{
    vec4 pos = worldMatrix * vec4(position, 1.0);

    gl_Position = projectionMatrix * pos;
    exColour = inColour;
    mvVertexPos = pos.xyz;
    mvVertexNormal = normalize(worldMatrix * vec4(vertexNormal, 0.0)).xyz;
}
