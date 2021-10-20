#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 inColour;
layout (location=2) in vec3 vertexNormal;

out vec3 exColour;
out vec3 mvVertexPos;
out vec3 mvVertexNormal;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;

    exColour = inColour;
    mvVertexPos = mvPos.xyz;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
}
