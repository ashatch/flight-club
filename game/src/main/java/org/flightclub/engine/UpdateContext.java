package org.flightclub.engine;

public record UpdateContext(
    float deltaTime,
    float timeMultiplier,
    Obj3dManager gameObjectManager
) {}
