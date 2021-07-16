package org.flightclub.engine.core;

import org.flightclub.engine.camera.Camera;
import org.joml.Vector2i;

public class RenderContext {
  private final Graphics graphics;
  private final Camera camera;
  private final Vector2i screenSize;

  private boolean isPaused;

  public RenderContext(
      final Graphics graphics,
      final Camera camera,
      final Vector2i screenSize,
      final boolean isPaused
  ) {
    this.graphics = graphics;
    this.camera = camera;
    this.screenSize = screenSize;
    this.isPaused = isPaused;
  }

  public Graphics graphics() {
    return graphics;
  }

  public Vector2i screenSize() {
    return screenSize;
  }

  public boolean isPaused() {
    return isPaused;
  }

  public void setPaused(boolean paused) {
    isPaused = paused;
  }

  public Camera camera() {
    return this.camera;
  }
}
