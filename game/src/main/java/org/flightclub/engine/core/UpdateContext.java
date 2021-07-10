package org.flightclub.engine.core;

public record UpdateContext(
    float deltaTime,
    float timeMultiplier,
    RenderManager gameObjectManager
) {}
