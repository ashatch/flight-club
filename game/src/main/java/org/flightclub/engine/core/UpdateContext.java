package org.flightclub.engine.core;

import org.flightclub.engine.RenderManager;

public record UpdateContext(
    float deltaTime,
    float timeMultiplier,
    RenderManager gameObjectManager
) {}
