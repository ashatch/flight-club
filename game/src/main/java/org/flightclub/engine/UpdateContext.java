package org.flightclub.engine;

public record UpdateContext(
    float deltaTime,
    Obj3dManager gameObjectManager
) {}
